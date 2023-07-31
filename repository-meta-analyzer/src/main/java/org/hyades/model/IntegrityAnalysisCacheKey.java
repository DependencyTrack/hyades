package org.hyades.model;

import java.io.Serializable;
import java.util.UUID;

public record IntegrityAnalysisCacheKey(String repositoryIdentifier, String url, UUID uuid, String purl) implements Serializable {
}
