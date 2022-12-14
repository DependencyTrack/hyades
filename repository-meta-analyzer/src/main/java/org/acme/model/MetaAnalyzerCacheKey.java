package org.acme.model;

import com.github.packageurl.PackageURL;
import org.acme.repositories.IMetaAnalyzer;

public class MetaAnalyzerCacheKey {

    private final IMetaAnalyzer analyzer;
    private final PackageURL purl;

    public MetaAnalyzerCacheKey(final IMetaAnalyzer analyzer, final PackageURL purl) {
        this.analyzer = analyzer;
        this.purl = purl;
    }
}
