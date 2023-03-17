package org.hyades.repositories;

import com.github.packageurl.PackageURL;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@ApplicationScoped
public class RepositoryAnalyzerFactory {

    private static final Map<String, Supplier<IMetaAnalyzer>> ANALYZER_SUPPLIERS = Map.of(
            PackageURL.StandardTypes.COMPOSER, ComposerMetaAnalyzer::new,
            PackageURL.StandardTypes.GEM, GemMetaAnalyzer::new,
            PackageURL.StandardTypes.GOLANG, GoModulesMetaAnalyzer::new,
            PackageURL.StandardTypes.HEX, HexMetaAnalyzer::new,
            PackageURL.StandardTypes.MAVEN, MavenMetaAnalyzer::new,
            PackageURL.StandardTypes.NPM, NpmMetaAnalyzer::new,
            PackageURL.StandardTypes.NUGET, NugetMetaAnalyzer::new,
            PackageURL.StandardTypes.PYPI, PypiMetaAnalyzer::new
    );

    private final CloseableHttpClient httpClient;

    RepositoryAnalyzerFactory(@Named("httpClient") final CloseableHttpClient httpClient) {
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
