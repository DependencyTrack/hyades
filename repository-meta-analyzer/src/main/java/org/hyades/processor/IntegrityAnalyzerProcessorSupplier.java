package org.hyades.processor;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.hyades.common.SecretDecryptor;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.repometaanalysis.v1.Component;
import org.hyades.proto.repometaanalysis.v1.IntegrityResult;
import org.hyades.repositories.IntegrityAnalyzerFactory;

@ApplicationScoped
public class IntegrityAnalyzerProcessorSupplier implements FixedKeyProcessorSupplier<String, Component, IntegrityResult> {
    private final RepoEntityRepository repoEntityRepository;
    private final IntegrityAnalyzerFactory integrityAnalyzerFactory;
    private final SecretDecryptor secretDecryptor;
    private final Cache cache;

    public IntegrityAnalyzerProcessorSupplier(final RepoEntityRepository repoEntityRepository,
                                              final IntegrityAnalyzerFactory integrityAnalyzerFactory,
                                              final SecretDecryptor secretDecryptor,
                                              @CacheName("integrityAnalyzer") final Cache cache) {
        this.repoEntityRepository = repoEntityRepository;
        this.integrityAnalyzerFactory = integrityAnalyzerFactory;
        this.secretDecryptor = secretDecryptor;
        this.cache = cache;
    }

    @Override
    public FixedKeyProcessor<String, Component, IntegrityResult> get() {
        return new IntegrityAnalyzerProcessor(repoEntityRepository, integrityAnalyzerFactory, secretDecryptor, cache);
    }
}
