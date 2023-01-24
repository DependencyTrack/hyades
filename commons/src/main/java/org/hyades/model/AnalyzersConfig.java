package org.hyades.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;

@RegisterForReflection
public record AnalyzersConfig(boolean snykEnabled, boolean OSSEnabled, boolean internalAnalyzerEnabled) implements Serializable {
}
