package org.acme.model;

import java.io.Serializable;

public record AnalyzersConfig(boolean snykEnabled, boolean OSSEnabled, boolean internalAnalyzerEnabled) implements Serializable {
}
