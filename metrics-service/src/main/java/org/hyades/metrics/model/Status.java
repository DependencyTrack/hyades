package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum Status {
    CREATED,
    UPDATED,
    DELETED,
    UNKNOWN,
    NO_CHANGE
}
