package org.dependencytrack.processor;

import com.github.packageurl.PackageURL;
import com.google.protobuf.Timestamp;
import io.quarkus.cache.Cache;
import io.quarkus.narayana.jta.QuarkusTransaction;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.model.IntegrityMeta;
import org.dependencytrack.model.MetaAnalyzerCacheKey;
import org.dependencytrack.model.MetaModel;
import org.dependencytrack.persistence.model.Component;
import org.dependencytrack.persistence.model.Repository;
import org.dependencytrack.persistence.model.RepositoryType;
import org.dependencytrack.persistence.repository.RepoEntityRepository;
import org.dependencytrack.proto.repometaanalysis.v1.AnalysisCommand;
import org.dependencytrack.proto.repometaanalysis.v1.AnalysisResult;
import org.dependencytrack.proto.repometaanalysis.v1.FetchMeta;
import org.dependencytrack.repositories.IMetaAnalyzer;
import org.dependencytrack.repositories.RepositoryAnalyzerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.dependencytrack.util.PurlUtil.parsePurlCoordinates;
import static org.dependencytrack.util.PurlUtil.parsePurlCoordinatesWithoutVersion;

class MetaAnalyzerProcessor extends ContextualFixedKeyProcessor<PackageURL, AnalysisCommand, AnalysisResult> {

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
    public void process(final FixedKeyRecord<PackageURL, AnalysisCommand> record) {
        final var analysisCommand = record.value();
        final var component = analysisCommand.getComponent();
        // NOTE: Do not use purlWithoutVersion for the analysis!
        // It only contains the type, namespace and name, but is missing the
        // version and other qualifiers. Some analyzers require the version.
        final PackageURL purl = parsePurlCoordinates(component.getPurl());

        final Optional<IMetaAnalyzer> optionalAnalyzer = analyzerFactory.createAnalyzer(purl);
        if (optionalAnalyzer.isEmpty()) {
            LOGGER.debug("No analyzer is capable of analyzing {}", purl);
            context().forward(record
                    .withValue(AnalysisResult.newBuilder().setComponent(component).build())
                    .withTimestamp(context().currentSystemTimeMs()));
            return;
        }

        final IMetaAnalyzer analyzer = optionalAnalyzer.get();
        var applicableRepositories = getApplicableRepositories(analyzer.supportedRepositoryType());

        AnalysisResult.Builder resultBuilder = AnalysisResult.newBuilder()
                .setComponent(component);

        if (analysisCommand.getFetchMeta().equals(FetchMeta.FETCH_META_LATEST_VERSION)
                || analysisCommand.getFetchMeta().equals(FetchMeta.FETCH_META_INTEGRITY_DATA_AND_LATEST_VERSION)) {
            resultBuilder = performRepoMeta(applicableRepositories, analyzer, analysisCommand, purl, resultBuilder);
            if (analysisCommand.getFetchMeta().equals(FetchMeta.FETCH_META_LATEST_VERSION)) {
                // forward result for only latest version
                context().forward(record
                        .withValue(resultBuilder.build())
                        .withTimestamp(context().currentSystemTimeMs()));
                return;
            }
        }

        if (analysisCommand.getFetchMeta().equals(FetchMeta.FETCH_META_INTEGRITY_DATA)
                || analysisCommand.getFetchMeta().equals(FetchMeta.FETCH_META_INTEGRITY_DATA_AND_LATEST_VERSION)) {
            resultBuilder = performIntegrityMeta(applicableRepositories, analyzer, analysisCommand, purl, resultBuilder);
            if (analysisCommand.getFetchMeta().equals(FetchMeta.FETCH_META_INTEGRITY_DATA)) {
                // forward result for only integrity meta
                context().forward(record
                        .withValue(resultBuilder.build())
                        .withTimestamp(context().currentSystemTimeMs()));
                return;
            }
        }
        // forward result for both latest version and integrity data OR even when no satisfactory results were yielded.
        context().forward(record
                .withValue(resultBuilder.build())
                .withTimestamp(context().currentSystemTimeMs()));
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

    private Optional<AnalysisResult> getCachedResult(final MetaAnalyzerCacheKey cacheKey) {
        try {
            final AnalysisResult cachedResult = cache.<MetaAnalyzerCacheKey, AnalysisResult>get(cacheKey,
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

    private void cacheResult(final MetaAnalyzerCacheKey cacheKey, final AnalysisResult result) {
        cache.get(cacheKey, key -> result).await().indefinitely();
    }

    private IntegrityMeta fetchIntegrityMeta(IMetaAnalyzer analyzer, final Repository repository, final AnalysisCommand analysisCommand) {
        configureAnalyzer(analyzer, repository);
        final var component = new Component();
        component.setPurl(analysisCommand.getComponent().getPurl());
        LOGGER.debug("Performing integrity meta fetch on purl: {}", component.getPurl());
        IntegrityMeta integrityMeta;
        try {
            integrityMeta = analyzer.getIntegrityMeta(component);
        } catch (UnsupportedOperationException unsupportedPackageException) {
            LOGGER.debug("Failed to analyze {} using {} with repository {} because package type is not supported",
                    component.getPurl(), analyzer.getName(), repository.getIdentifier(), unsupportedPackageException);
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to analyze {} using {} with repository {}",
                    component.getPurl(), analyzer.getName(), repository.getIdentifier(), e);
            return null;
        }
        LOGGER.debug("Found integrity metadata for: {} using repository: {} ({})",
                component.getPurl(), repository.getIdentifier(), repository.getType());
        return integrityMeta;
    }

    private MetaModel fetchRepoMeta(IMetaAnalyzer analyzer, final Repository repository, final AnalysisCommand analysisCommand) {
        configureAnalyzer(analyzer, repository);
        // Analyzers still work with "legacy" data models,
        // allowing us to avoid major refactorings of the original code.
        final var component = new Component();
        component.setPurl(analysisCommand.getComponent().getPurl());
        LOGGER.debug("Performing meta analysis on purl: {}", component.getPurl());
        MetaModel metaModel = null;
        try {
            metaModel = analyzer.analyze(component);
            LOGGER.debug("Found component metadata for: {} using repository: {} ({})",
                    component.getPurl(), repository.getIdentifier(), repository.getType());
        } catch (Exception e) {
            LOGGER.error("Failed to analyze {} using {} with repository {}",
                    component.getPurl(), analyzer.getName(), repository.getIdentifier(), e);
        }
        return metaModel;
    }

    private void configureAnalyzer(final IMetaAnalyzer analyzer, final Repository repository) {
        analyzer.setRepositoryBaseUrl(repository.getUrl());
        boolean isAuthenticationRequired = Optional.ofNullable(repository.isAuthenticationRequired()).orElse(false);
        if (isAuthenticationRequired) {
            try {
                analyzer.setRepositoryUsernameAndPassword(repository.getUsername(), secretDecryptor.decryptAsString(repository.getPassword()));
            } catch (Exception e) {
                LOGGER.error("Failed decrypting password for repository: " + repository.getIdentifier(), e);
            }
        }
    }

    private AnalysisResult.Builder performRepoMeta(List<Repository> applicableRepositories, final IMetaAnalyzer analyzer, final AnalysisCommand analysisCommand,
                                                   final PackageURL componentPurl, AnalysisResult.Builder resultBuilder) {
        final var component = analysisCommand.getComponent();
        for (Repository repository : applicableRepositories) {
            if ((repository.isInternal() && !component.getInternal())
                    || (!repository.isInternal() && component.getInternal())) {
                // Internal components should only be analyzed using internal repositories.
                // Non-internal components should only be analyzed with non-internal repositories.
                // We do not want non-internal components being analyzed with internal repositories as
                // internal repositories are not the source of truth for these components, even if the
                // repository acts as a proxy to the source of truth. This cannot be assumed.
                LOGGER.debug("Skipping component with purl {} ", component.getPurl());
                continue;
            }
            resultBuilder.setRepository(repository.getIdentifier());
            var cacheKeyWithoutVersion = new MetaAnalyzerCacheKey(analyzer.getName(), parsePurlCoordinatesWithoutVersion(component.getPurl()).canonicalize(), repository.getUrl());
            var cachedResult = getCachedResult(cacheKeyWithoutVersion);
            if (cachedResult.isPresent()) {
                LOGGER.debug("Cache hit for latest version (analyzer: {}, purl: {}, repository: {})", analyzer.getName(), componentPurl, repository.getIdentifier());
                resultBuilder.setLatestVersion(cachedResult.get().getLatestVersion());
                resultBuilder.setPublished(cachedResult.get().getPublished());
                break;
            } else {
                LOGGER.debug("Cache miss for latest version (analyzer: {}, purl: {}, repository: {})", analyzer.getName(), componentPurl, repository.getIdentifier());
                final var repoMeta = fetchRepoMeta(analyzer, repository, analysisCommand);
                if (repoMeta != null && repoMeta.getLatestVersion() != null && !repoMeta.getLatestVersion().isEmpty()) {
                    Optional.ofNullable(repoMeta.getLatestVersion()).ifPresent(
                            version -> resultBuilder.setLatestVersion(version));
                    Optional.ofNullable(repoMeta.getPublishedTimestamp()).ifPresent(
                            version -> resultBuilder.setPublished(Timestamp.newBuilder()
                                    .setSeconds(repoMeta.getPublishedTimestamp().getTime() / 1000)));
                    cacheResult(cacheKeyWithoutVersion, resultBuilder.build());
                    break;
                }
            }
        }
        return resultBuilder;
    }

    private AnalysisResult.Builder performIntegrityMeta(List<Repository> applicableRepositories, final IMetaAnalyzer analyzer, final AnalysisCommand analysisCommand,
                                                        final PackageURL componentPurl, AnalysisResult.Builder resultBuilder) {
        final var component = analysisCommand.getComponent();
        for (Repository repository : applicableRepositories) {
            if ((repository.isInternal() && !component.getInternal())
                    || (!repository.isInternal() && component.getInternal())) {
                LOGGER.debug("Skipping component with purl {} ", component.getPurl());
                continue;
            }
            var cacheKeyWithVersion = new MetaAnalyzerCacheKey(analyzer.getName(), parsePurlCoordinates(component.getPurl()).canonicalize(), repository.getUrl());
            var cachedResult = getCachedResult(cacheKeyWithVersion);
            if (cachedResult.isPresent()) {
                LOGGER.debug("Cache hit for integrity meta (analyzer: {}, purl: {}, repository: {})", analyzer.getName(), componentPurl, repository.getIdentifier());
                resultBuilder.setIntegrityMeta(cachedResult.get().getIntegrityMeta());
                break;
            } else {
                LOGGER.debug("Cache miss for integrity meta (analyzer: {}, purl: {}, repository: {})", analyzer.getName(), componentPurl, repository.getIdentifier());
                var integrityMeta = fetchIntegrityMeta(analyzer, repository, analysisCommand);
                if (integrityMeta != null) {
                    var metaBuilder = org.dependencytrack.proto.repometaanalysis.v1.IntegrityMeta.newBuilder();
                    Optional.ofNullable(integrityMeta.getMd5()).ifPresent(hash -> metaBuilder.setMd5(hash));
                    Optional.ofNullable(integrityMeta.getSha1()).ifPresent(hash -> metaBuilder.setSha1(hash));
                    Optional.ofNullable(integrityMeta.getSha256()).ifPresent(hash -> metaBuilder.setSha256(hash));
                    Optional.ofNullable(integrityMeta.getSha512()).ifPresent(hash -> metaBuilder.setSha512(hash));
                    Optional.ofNullable(integrityMeta.getMetaSourceUrl()).ifPresent(url -> metaBuilder.setMetaSourceUrl(url));
                    Optional.ofNullable(integrityMeta.getCurrentVersionLastModified()).ifPresent(date ->
                            metaBuilder.setCurrentVersionLastModified(Timestamp.newBuilder()
                                    .setSeconds(date.getTime() / 1000)));
                    resultBuilder.setIntegrityMeta(metaBuilder);
                    cacheResult(cacheKeyWithVersion, resultBuilder.build());
                    break;
                }
            }
        }
        return resultBuilder;
    }
}
