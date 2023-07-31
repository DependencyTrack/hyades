package org.hyades.repositories;

import com.github.packageurl.PackageURL;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@ApplicationScoped
public class IntegrityAnalyzerFactory {
    private static final Map<String, Supplier<IMetaAnalyzer>> ANALYZER_SUPPLIERS = Map.of(
            PackageURL.StandardTypes.MAVEN, MavenMetaAnalyzer::new,
            PackageURL.StandardTypes.NPM, NpmMetaAnalyzer::new,
            PackageURL.StandardTypes.PYPI, PypiMetaAnalyzer::new
    );
    private final CloseableHttpClient httpClient;

    IntegrityAnalyzerFactory(@Named("httpClient") final CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean hasApplicableAnalyzer(final PackageURL purl) {
        return ANALYZER_SUPPLIERS.containsKey(purl.getType());
    }

    public Optional<IMetaAnalyzer> createAnalyzer(final PackageURL purl) {
        final Supplier<IMetaAnalyzer> analyzerSupplier = ANALYZER_SUPPLIERS.get(purl.getType());
        if (analyzerSupplier == null) {
            return Optional.empty();
        }


        final IMetaAnalyzer analyzer = analyzerSupplier.get();
        analyzer.setHttpClient(httpClient);
        return Optional.of(analyzer);
    }
}
