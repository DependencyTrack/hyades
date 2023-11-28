package org.dependencytrack.processor;

import com.github.packageurl.PackageURL;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.persistence.repository.RepoEntityRepository;
import org.dependencytrack.proto.repometaanalysis.v1.AnalysisCommand;
import org.dependencytrack.proto.repometaanalysis.v1.AnalysisResult;
import org.dependencytrack.repositories.RepositoryAnalyzerFactory;

@ApplicationScoped
public class MetaAnalyzerProcessorSupplier implements FixedKeyProcessorSupplier<PackageURL, AnalysisCommand, AnalysisResult> {

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
    public FixedKeyProcessor<PackageURL, AnalysisCommand, AnalysisResult> get() {
        return new MetaAnalyzerProcessor(repoEntityRepository, analyzerFactory, secretDecryptor, cache);
    }

}
