package org.hyades.processor;

import com.github.packageurl.PackageURL;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.hyades.common.SecretDecryptor;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.repometaanalysis.v1.AnalysisResult;
import org.hyades.proto.repometaanalysis.v1.Component;
import org.hyades.repositories.RepositoryAnalyzerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MetaAnalyzerProcessorSupplier implements FixedKeyProcessorSupplier<PackageURL, Component, AnalysisResult> {

    private final RepoEntityRepository repoEntityRepository;
    private final RepositoryAnalyzerFactory analyzerFactory;
    private final SecretDecryptor secretDecryptor;
    private final Cache cache;

    public MetaAnalyzerProcessorSupplier(final RepoEntityRepository repoEntityRepository,
                                         final RepositoryAnalyzerFactory analyzerFactory,
                                         final SecretDecryptor secretDecryptor,
                                         @CacheName("metaAnalyzer") final Cache cache) {
        this.repoEntityRepository = repoEntityRepository;
        this.analyzerFactory = analyzerFactory;
        this.secretDecryptor = secretDecryptor;
        this.cache = cache;
    }

    @Override
    public FixedKeyProcessor<PackageURL, Component, AnalysisResult> get() {
        return new MetaAnalyzerProcessor(repoEntityRepository, analyzerFactory, secretDecryptor, cache);
    }

}
