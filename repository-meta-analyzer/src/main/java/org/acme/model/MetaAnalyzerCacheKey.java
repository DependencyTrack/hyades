package org.acme.model;

import java.io.Serializable;

public record MetaAnalyzerCacheKey(String analyzer, String purl) implements Serializable {
}
