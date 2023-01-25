package org.hyades.common;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes of Caffeine cache for reflection.
 * <p>
 * Fixes {@link ClassNotFoundException} when running as native image.
 *
 * @see <a href="https://quarkus.io/guides/cache#going-native">Quarkus Cache docs</a>
 */
@SuppressWarnings("unused")
@RegisterForReflection(classNames = {
        "com.github.benmanes.caffeine.cache.SSSW"
})
public class CaffeineReflectionConfiguration {
}
