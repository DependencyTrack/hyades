package org.hyades.model;

import java.io.Serializable;

public record IntegrityAnalysisCacheKey(String repositoryIdentifier, String url, String purl) implements Serializable {
}
