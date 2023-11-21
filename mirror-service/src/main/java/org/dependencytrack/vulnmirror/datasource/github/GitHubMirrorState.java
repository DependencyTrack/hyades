package org.dependencytrack.vulnmirror.datasource.github;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GitHubMirrorState(long lastUpdatedEpochSeconds) {
}
