package org.hyades.vulnmirror.datasource.github;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record GitHubMirrorState(long lastUpdatedEpochSeconds) {
}
