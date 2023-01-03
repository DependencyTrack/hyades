package org.acme.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;

@RegisterForReflection
public record MetaAnalyzerCacheKey(String analyzer, String purl) implements Serializable {
}
