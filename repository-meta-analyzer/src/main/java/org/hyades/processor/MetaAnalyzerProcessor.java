package org.hyades.processor;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.google.protobuf.Timestamp;
import io.quarkus.cache.Cache;
import io.quarkus.narayana.jta.QuarkusTransaction;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.hyades.common.SecretDecryptor;
import org.hyades.model.MetaAnalyzerCacheKey;
import org.hyades.model.MetaModel;
import org.hyades.model.Repository;
import org.hyades.model.RepositoryType;
import org.hyades.persistence.RepoEntityRepository;
import org.hyades.proto.repometaanalysis.v1.Component;
import org.hyades.proto.repometaanalysis.v1.Result;
import org.hyades.repositories.IMetaAnalyzer;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

class MetaAnalyzerProcessor extends ContextualFixedKeyProcessor<PackageURL, Component, Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaAnalyzerProcessor.class);

    private final RepoEntityRepository repoEntityRepository;
    private final RepositoryAnalyzerFactory analyzerFactory;
    private final SecretDecryptor secretDecryptor;
    private final Cache cache;

    MetaAnalyzerProcessor(final RepoEntityRepository repoEntityRepository,
                          final RepositoryAnalyzerFactory analyzerFactory,
                          final SecretDecryptor secretDecryptor,
                          final Cache cache) {
        this.repoEntityRepository = repoEntityRepository;
        this.analyzerFactory = analyzerFactory;
        this.secretDecryptor = secretDecryptor;
        this.cache = cache;
    }

    @Override
    public void process(final FixedKeyRecord<PackageURL, Component> record) {
        final Component component = record.value();

        // NOTE: Do not use the PURL from the record's key for the analysis!
        // It only contains the type, namespace and name, but is missing the
        // version and other qualifiers. Some analyzers require the version.
        final PackageURL purl = mustParsePurl(component.getPurl());

        final Optional<IMetaAnalyzer> optionalAnalyzer = analyzerFactory.createAnalyzer(purl);
        if (optionalAnalyzer.isEmpty()) {
            LOGGER.debug("No analyzer is capable of analyzing {}", purl);
            context().forward(record
                    .withValue(Result.newBuilder().setComponent(component).build())
                    .withTimestamp(context().currentSystemTimeMs()));
            return;
        }

        final IMetaAnalyzer analyzer = optionalAnalyzer.get();

        for (Repository repository : getApplicableRepositories(analyzer.supportedRepositoryType())) {
            if ((repository.isInternal() && !component.getInternal())
                    || (!repository.isInternal() && component.getInternal())) {
                // Internal components should only be analyzed using internal repositories.
                // Non-internal components should only be analyzed with non-internal repositories.
                // We do not want non-internal components being analyzed with internal repositories as
                // internal repositories are not the source of truth for these components, even if the
                // repository acts as a proxy to the source of truth. This cannot be assumed.
                continue;
            }

            // TODO: Can we use the PURL coordinates without version here?
            // That would result in many more cache hits, but it will not work anymore as soon
            // as we also want to capture version-specific data, like checksums.
            final var cacheKey = new MetaAnalyzerCacheKey(analyzer.getName(), purl.getCoordinates(), repository.getUrl());

            // Populate results from cache if possible.
            final var cachedResult = getCachedResult(cacheKey);
            if (cachedResult.isPresent()) {
                LOGGER.debug("Cache hit (analyzer: {}, purl: {}, repository: {})", analyzer.getName(), purl, repository.getIdentifier());
                context().forward(record
                        .withValue(cachedResult.get())
                        .withTimestamp(context().currentSystemTimeMs()));
                return;
            } else {
                LOGGER.debug("Cache miss (analyzer: {}, purl: {}, repository: {})", analyzer.getName(), purl, repository.getIdentifier());
            }

            analyzer.setRepositoryBaseUrl(repository.getUrl());
            if (repository.isInternal()) {
                try {
                    analyzer.setRepositoryUsernameAndPassword(repository.getUsername(), secretDecryptor.decryptAsString(repository.getPassword()));
                } catch (Exception e) {
                    LOGGER.error("Failed decrypting password for repository: " + repository.getIdentifier(), e);
                }
            }

            LOGGER.debug("Performing meta analysis on purl: {}", purl);
            final MetaModel metaModel;
            try {
                // Analyzers still work with "legacy" data models,
                // allowing us to avoid major refactorings of the original code.
                final var analyzerComponent = new org.hyades.model.Component();
                analyzerComponent.setPurl(purl);
                metaModel = analyzer.analyze(analyzerComponent);
            } catch (Exception e) {
                LOGGER.error("Failed to analyze {} using {} with repository {}", purl, analyzer.getName(), repository.getIdentifier(), e);
                continue;
            }

            final Result.Builder resultBuilder = Result.newBuilder().setComponent(component);
            if (metaModel.getLatestVersion() != null) {
                resultBuilder.setLatestVersion(metaModel.getLatestVersion());
                if (metaModel.getPublishedTimestamp() != null) {
                    resultBuilder.setPublished(Timestamp.newBuilder()
                            .setSeconds(metaModel.getPublishedTimestamp().getTime() / 1000));
                }
                final Result result = resultBuilder.build();
                context().forward(record.withValue(result).withTimestamp(context().currentSystemTimeMs()));
                cacheResult(cacheKey, result);
                LOGGER.debug("Found component metadata for: {} using repository: {} ({})",
                        purl, repository.getIdentifier(), repository.getType());
                return;
            }
        }

        // Produce "empty" result in case no repository did yield a satisfactory result.
        context().forward(record
                .withValue(Result.newBuilder().setComponent(component).build())
                .withTimestamp(context().currentSystemTimeMs()));
    }

    private PackageURL mustParsePurl(final String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Repository> getApplicableRepositories(final RepositoryType repositoryType) {
        // Hibernate requires an active transaction to perform any sort of interaction
        // with the database. Because processors can't be CDI beans, we cannot use
        // @Transactional and the like. Falling back to manual transaction management.
        //
        // NOTE: The result of this query can potentially be cached for at least a few minutes.
        // Executing it for every single component feels excessive.
        // Quarkus has Hibernate L2 cache enabled by default, we just need to opt in to using
        // it for this query: https://quarkus.io/guides/hibernate-orm#caching-of-queries
        // Should be tested whether throughput can be improved this way.
        return QuarkusTransaction.requiringNew()
                .call(() -> repoEntityRepository.findEnabledRepositoriesByType(repositoryType));
    }

    private Optional<Result> getCachedResult(final MetaAnalyzerCacheKey cacheKey) {
        try {
            final Result cachedResult = cache.<MetaAnalyzerCacheKey, Result>get(cacheKey,
                    key -> {
                        // null values would be cached, so throw an exception instead.
                        // See https://quarkus.io/guides/cache#let-exceptions-bubble-up
                        throw new NoSuchElementException();
                    }).await().indefinitely();
            return Optional.of(cachedResult);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void cacheResult(final MetaAnalyzerCacheKey cacheKey, final Result result) {
        cache.get(cacheKey, key -> result).await().indefinitely();
    }

}
