package org.acme.model;

import java.io.Serializable;

public class MetaAnalyzerCacheKey implements Serializable {

    private transient String analyzer;
    private transient String purl;

    public MetaAnalyzerCacheKey(final String analyzer, final String purl) {
        this.analyzer = analyzer;
        this.purl = purl;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public String getPurl() {
        return purl;
    }
}
