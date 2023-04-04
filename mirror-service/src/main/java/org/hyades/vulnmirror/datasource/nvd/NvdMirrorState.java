package org.hyades.vulnmirror.datasource.nvd;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record NvdMirrorState(long lastModifiedEpochSeconds) {
}
