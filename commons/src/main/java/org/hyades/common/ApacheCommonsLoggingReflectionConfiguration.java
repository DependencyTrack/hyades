package org.hyades.common;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * Register classes of Apache Commons Logging for reflection.
 * <p>
 * Fixes {@link ClassNotFoundException} when running as native image.
 *
 * @see <a href="https://github.com/quarkusio/quarkus/issues/10128">Quarkus issue #10128</a>
 */
@SuppressWarnings("unused")
@RegisterForReflection(targets = {
        LogFactory.class,
        SimpleLog.class
})
public class ApacheCommonsLoggingReflectionConfiguration {
}
