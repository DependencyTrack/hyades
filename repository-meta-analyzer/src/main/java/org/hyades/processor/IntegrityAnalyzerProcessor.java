package org.hyades.processor;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.google.protobuf.Timestamp;
import io.quarkus.cache.Cache;
import io.quarkus.narayana.jta.QuarkusTransaction;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.hyades.common.SecretDecryptor;
import org.hyades.model.IntegrityAnalysisCacheKey;
import org.hyades.model.IntegrityModel;
import org.hyades.persistence.model.Repository;
import org.hyades.persistence.model.RepositoryType;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.repometaanalysis.v1.Component;
import org.hyades.proto.repometaanalysis.v1.HashMatchStatus;
import org.hyades.proto.repometaanalysis.v1.IntegrityResult;
import org.hyades.repositories.IMetaAnalyzer;
import org.hyades.repositories.IntegrityAnalyzerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class IntegrityAnalyzerProcessor extends ContextualFixedKeyProcessor<String, Component, IntegrityResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrityAnalyzerProcessor.class);
    private final RepoEntityRepository repoEntityRepository;
    private final IntegrityAnalyzerFactory integrityAnalyzerFactory;
    private final SecretDecryptor secretDecryptor;
    private final Cache cache;

    IntegrityAnalyzerProcessor(final RepoEntityRepository repoEntityRepository,
                               final IntegrityAnalyzerFactory integrityAnalyzerFactory,
                               final SecretDecryptor secretDecryptor,
                               final Cache cache) {
        this.repoEntityRepository = repoEntityRepository;
        this.integrityAnalyzerFactory = integrityAnalyzerFactory;
        this.secretDecryptor = secretDecryptor;
        this.cache = cache;
    }

    @Override
    public void process(FixedKeyRecord<String, Component> record) {
        final Component component = record.value();
        IntegrityResult result;
        // NOTE: Do not use purlWithoutVersion for the analysis!
        // It only contains the type, namespace and name, but is missing the
        // version and other qualifiers. Some analyzers require the version.
        final PackageURL purl = mustParsePurl(component.getPurl());
        final Optional<IMetaAnalyzer> optionalAnalyzer = integrityAnalyzerFactory.createAnalyzer(purl);
        if (optionalAnalyzer.isEmpty()) {
            LOGGER.debug("No analyzer is capable of analyzing {}", purl);
            context().forward(record
                    .withValue(IntegrityResult.newBuilder().setComponent(component).build())
                    .withTimestamp(context().currentSystemTimeMs()));
            return;
        }

        final IMetaAnalyzer analyzer = optionalAnalyzer.get();
        for (Repository repository : getApplicableRepositories(analyzer.supportedRepositoryType())) {
            if (repository.isIntegrityCheckEnabled()) {
                if ((component.hasMd5Hash() || component.hasSha256Hash() || component.hasSha1Hash())) {
                    LOGGER.info("Will perform integrity check for received component:  {} for repository: {}", component.getPurl(), repository.getIdentifier());
                    result = performIntegrityCheckForComponent(analyzer, repository, component);
                } else {
                    final IntegrityResult.Builder resultBuilder = IntegrityResult.newBuilder()
                            .setMd5HashMatch(HashMatchStatus.COMPONENT_MISSING_HASH)
                            .setComponent(component)
                            .setSha1HashMatch(HashMatchStatus.COMPONENT_MISSING_HASH)
                            .setSha256Match(HashMatchStatus.COMPONENT_MISSING_HASH)
                            .setRepository(repository.getIdentifier());
                    result = resultBuilder.build();

                }
                context().forward(record.withValue(result).withTimestamp(context().currentSystemTimeMs()));
                return;
            }
        }


    }

    private PackageURL mustParsePurl(final String purl) {
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            throw new IllegalStateException("""
                    The provided PURL is invalid, even though it should have been
                    validated in a previous processing step
                    """, e);
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
        //changed this to joinexisting because with new transaction, it is not fetching values that were inserted from
        // existing previous transaction and returning empty result
        return QuarkusTransaction.joiningExisting()
                .call(() -> repoEntityRepository.findEnabledRepositoriesByType(repositoryType));
    }

    private IntegrityResult performIntegrityCheckForComponent(final IMetaAnalyzer analyzer, final Repository repository, final Component component) {

        final var integrityResultCacheKey = new IntegrityAnalysisCacheKey(repository.getIdentifier(), repository.getUrl(), UUID.fromString(component.getUuid()), component.getPurl());
        final var integrityAnalysisCacheResult = getCachedResult(integrityResultCacheKey);
        if (integrityAnalysisCacheResult != null) {
            LOGGER.debug("Cache hit for integrity result (analyzer: {}, url: {}, component purl: {})", analyzer.getName(), repository.getUrl(), component.getPurl());
            return integrityAnalysisCacheResult;
        } else {
            LOGGER.debug("Cache miss for integrity check (analyzer: {}, purl: {}, repository: {})", analyzer.getName(), repository.getUrl(), component.getUuid());
            analyzer.setRepositoryBaseUrl(repository.getUrl());
            if (Boolean.TRUE.equals(repository.isAuthenticationRequired())) {
                try {
                    analyzer.setRepositoryUsernameAndPassword(repository.getUsername(), secretDecryptor.decryptAsString(repository.getPassword()));
                } catch (Exception e) {
                    LOGGER.error("Failed decrypting password for repository: " + repository.getIdentifier(), e);
                }
            }

            LOGGER.debug("Performing integrity check on component: {}", component.getPurl());
            final IntegrityModel integrityModel;
            try {
                // Analyzers still work with "legacy" data models,
                // allowing us to avoid major refactorings of the original code.
                final var analyzerComponent = new org.hyades.persistence.model.Component();
                analyzerComponent.setPurl(component.getPurl());
                analyzerComponent.setMd5(component.getMd5Hash());
                analyzerComponent.setSha1(component.getSha1Hash());
                analyzerComponent.setSha256(component.getSha256Hash());
                UUID uuid = UUID.fromString(component.getUuid());
                analyzerComponent.setUuid(uuid);
                analyzerComponent.setId((long) component.getComponentId());
                integrityModel = analyzer.checkIntegrityOfComponent(analyzerComponent);
            } catch (Exception e) {
                LOGGER.error("Failed to analyze {} using {} with repository {}",
                        component.getPurl(), analyzer.getName(), repository.getIdentifier(), e);
                return null;
            }
            Component component1 = Component.newBuilder().setPurl(integrityModel.getComponent().getPurl().toString())
                    .setInternal(integrityModel.getComponent().isInternal())
                    .setMd5Hash(integrityModel.getComponent().getMd5())
                    .setSha1Hash(integrityModel.getComponent().getSha1())
                    .setSha256Hash(integrityModel.getComponent().getSha256())
                    .setUuid(integrityModel.getComponent().getUuid().toString())
                    .setComponentId(integrityModel.getComponent().getId()).build();
            final IntegrityResult.Builder resultBuilder = IntegrityResult.newBuilder()
                    .setMd5HashMatch(integrityModel.isMd5HashMatched())
                    .setComponent(component1)
                    .setSha1HashMatch(integrityModel.isSha1HashMatched())
                    .setSha256Match(integrityModel.isSha256HashMatched())
                    .setRepository(repository.getUrl())
                    .setPublished(Timestamp.newBuilder().setSeconds(System.currentTimeMillis()));
            cacheResult(integrityResultCacheKey, resultBuilder.build());
            return resultBuilder.build();
        }
    }

    private void cacheResult(final IntegrityAnalysisCacheKey cacheKey, final IntegrityResult result) {
        cache.get(cacheKey, key -> result).await().indefinitely();
    }

    private IntegrityResult getCachedResult(final IntegrityAnalysisCacheKey cacheKey) {
        try {
            return cache.<IntegrityAnalysisCacheKey, IntegrityResult>get(cacheKey,
                    key -> {
                        // null values would be cached, so throw an exception instead.
                        // See https://quarkus.io/guides/cache#let-exceptions-bubble-up
                        throw new NoSuchElementException();
                    }).await().indefinitely();
        } catch (Exception e) {
            return null;
        }
    }
}