package org.acme.model;

import com.github.packageurl.PackageURL;
import org.acme.repositories.IMetaAnalyzer;

public class MetaAnalyzerCacheKey {

    private IMetaAnalyzer analyzer;
    private PackageURL purl;

    public MetaAnalyzerCacheKey(final IMetaAnalyzer analyzer, final PackageURL purl) {
        this.analyzer = analyzer;
        this.purl = purl;
    }

    public IMetaAnalyzer getAnalyzer() {
        return analyzer;
    }

    public PackageURL getPurl() {
        return purl;
    }
}
