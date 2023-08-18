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
import org.hyades.repositories.IntegrityAnalyzer;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class IntegrityAnalyzerProcessor extends ContextualFixedKeyProcessor<PackageURL, Component, IntegrityResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrityAnalyzerProcessor.class);
    private final RepoEntityRepository repoEntityRepository;
    private final RepositoryAnalyzerFactory integrityAnalyzerFactory;
    private final SecretDecryptor secretDecryptor;
    private final Cache cache;

    IntegrityAnalyzerProcessor(final RepoEntityRepository repoEntityRepository,
                               final RepositoryAnalyzerFactory integrityAnalyzerFactory,
                               final SecretDecryptor secretDecryptor,
                               final Cache cache) {
        this.repoEntityRepository = repoEntityRepository;
        this.integrityAnalyzerFactory = integrityAnalyzerFactory;
        this.secretDecryptor = secretDecryptor;
        this.cache = cache;
    }

    @Override
    public void process(FixedKeyRecord<PackageURL, Component> inputRecord) {
        final Component component = inputRecord.value();
        IntegrityResult result;
        // NOTE: Do not use purlWithoutVersion for the analysis!
        // It only contains the type, namespace and name, but is missing the
        // version and other qualifiers. Some analyzers require the version.
        final PackageURL purl = mustParsePurl(component.getPurl());
        final Optional<IntegrityAnalyzer> integrityAnalyzer = integrityAnalyzerFactory.createIntegrityAnalyzer(purl);
        if (integrityAnalyzer.isEmpty()) {
            LOGGER.debug("No analyzer is capable of analyzing {}", purl);
            context().forward(inputRecord
                    .withValue(IntegrityResult.newBuilder().setComponent(component).build())
                    .withTimestamp(context().currentSystemTimeMs()));
            return;
        }

        final IntegrityAnalyzer analyzer = integrityAnalyzer.get();
        for (Repository repository : getApplicableRepositories(analyzer.supportedRepositoryType())) {
            if (repository.isIntegrityCheckEnabled()) {
                if ((component.hasMd5Hash() || component.hasSha256Hash() || component.hasSha1Hash())) {
                    LOGGER.debug("Will perform integrity check for received component:  {} for repository: {}", component.getPurl(), repository.getIdentifier());
                    result = performIntegrityCheckForComponent(analyzer, repository, component);
                } else {
                    final IntegrityResult.Builder resultBuilder = IntegrityResult.newBuilder()
                            .setMd5HashMatch(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH)
                            .setComponent(component)
                            .setSha1HashMatch(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH)
                            .setSha256HashMatch(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH)
                            .setRepositoryUrl(repository.getIdentifier());
                    result = resultBuilder.build();

                }
                if (result != null) {
                    context().forward(inputRecord.withValue(result).withTimestamp(context().currentSystemTimeMs()));
                }
                return;
            }
        }
        // Produce "empty" result in case no repository did yield a satisfactory result.
        context().forward(inputRecord
                .withValue(IntegrityResult.newBuilder().setComponent(component)
                        .setMd5HashMatch(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN).setSha1HashMatch(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN)
                        .setSha256HashMatch(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN).build())
                .withTimestamp(context().currentSystemTimeMs()));
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

    private IntegrityResult performIntegrityCheckForComponent(final IntegrityAnalyzer analyzer, final Repository repository, final Component component) {

        final var integrityResultCacheKey = new IntegrityAnalysisCacheKey(repository.getIdentifier(), repository.getUrl(), component.getPurl());
        final var integrityAnalysisCacheResult = getCachedIntegrityModel(integrityResultCacheKey);
        final org.hyades.persistence.model.Component persistentComponent = createPersistentComponent(analyzer, repository, component);

        if (persistentComponent == null) return null;

        if (integrityAnalysisCacheResult != null) {
            LOGGER.debug("Cache hit for integrity result (analyzer: {}, url: {}, component purl: {})", analyzer.getName(), repository.getUrl(), component.getPurl());
            try {
                return getIntegrityResult(repository, integrityAnalysisCacheResult);
            } catch (Exception ex) {
                LOGGER.error("Failed to analyze {} using {} with repository {}",
                        component.getPurl(), analyzer.getName(), repository.getIdentifier(), ex);
                return null;
            }

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
            try {
                var integrityModel = analyzer.getIntegrityModel(persistentComponent);
                cacheIntegrityModel(integrityResultCacheKey, integrityModel);
                return getIntegrityResult(repository, integrityModel);
            } catch (Exception ex) {
                LOGGER.error("Failed to perform integrity check on component with purl:{} {}", component.getPurl(), ex);
            }
            return null;
        }
    }

    private org.hyades.persistence.model.Component createPersistentComponent(IntegrityAnalyzer analyzer, Repository repository, Component component) {
        final var persistentComponent = new org.hyades.persistence.model.Component();
        try {
            persistentComponent.setPurl(component.getPurl());
            persistentComponent.setMd5(component.getMd5Hash());
            persistentComponent.setSha1(component.getSha1Hash());
            persistentComponent.setSha256(component.getSha256Hash());
            UUID uuid = UUID.fromString(component.getUuid());
            persistentComponent.setUuid(uuid);
        } catch (Exception e) {
            LOGGER.error("Failed to analyze {} using {} with repository {}",
                    component.getPurl(), analyzer.getName(), repository.getIdentifier(), e);
            return null;
        }
        return persistentComponent;
    }

    private IntegrityResult getIntegrityResult(Repository repository, IntegrityModel integrityModel) {
        Component analyzerComponent = Component.newBuilder().setPurl(integrityModel.getComponent().getPurl().toString())
                .setInternal(integrityModel.getComponent().isInternal())
                .setMd5Hash(integrityModel.getComponent().getMd5())
                .setSha1Hash(integrityModel.getComponent().getSha1())
                .setSha256Hash(integrityModel.getComponent().getSha256())
                .setUuid(integrityModel.getComponent().getUuid().toString()).build();

        final IntegrityResult.Builder resultBuilder = IntegrityResult.newBuilder()
                .setMd5HashMatch(integrityModel.isMd5HashMatched())
                .setComponent(analyzerComponent)
                .setSha1HashMatch(integrityModel.isSha1HashMatched())
                .setSha256HashMatch(integrityModel.isSha256HashMatched())
                .setRepositoryUrl(repository.getUrl())
                .setUpdated(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()));
        return resultBuilder.build();
    }

    private void cacheIntegrityModel(final IntegrityAnalysisCacheKey cacheKey, final IntegrityModel integrityModel) {
        cache.get(cacheKey, key -> integrityModel).await().indefinitely();
    }

    private IntegrityModel getCachedIntegrityModel(final IntegrityAnalysisCacheKey cacheKey) {
        try {
            return cache.<IntegrityAnalysisCacheKey, IntegrityModel>get(cacheKey,
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
