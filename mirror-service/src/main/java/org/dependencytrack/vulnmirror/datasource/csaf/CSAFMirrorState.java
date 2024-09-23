package org.dependencytrack.vulnmirror.datasource.csaf;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record CSAFMirrorState(long lastModifiedEpochSeconds) {
}
