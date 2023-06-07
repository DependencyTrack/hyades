package org.hyades.notification.config;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Reflection configuration for UniJ classes.
 * <p>
 * UniJ is used by the Confluent Parallel Consumer.
 */
@SuppressWarnings("unused")
@RegisterForReflection(classNames = {
        "pl.tlinkowski.unij.service.collect.jdk8.Jdk8UnmodifiableListFactory",
        "pl.tlinkowski.unij.service.collect.jdk8.Jdk8UnmodifiableMapFactory",
        "pl.tlinkowski.unij.service.collect.jdk8.Jdk8UnmodifiableSetFactory"
})
public class UniJReflectionConfiguration {
}
