package org.acme.model;

import com.github.packageurl.PackageURL;
import org.acme.repositories.IMetaAnalyzer;

public class MetaAnalyzerCacheKey {

    private IMetaAnalyzer analyzer;
    private PackageURL purl;

    public MetaAnalyzerCacheKey(IMetaAnalyzer analyzer, PackageURL purl) {
        this.analyzer = analyzer;
        this.purl = purl;
    }
}
