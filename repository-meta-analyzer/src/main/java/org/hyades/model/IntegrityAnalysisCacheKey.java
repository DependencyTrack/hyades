package org.hyades.model;

import java.io.Serializable;
import java.util.UUID;

public record IntegrityAnalysisCacheKey(String analyzer, String url, UUID uuid) implements Serializable {
}
