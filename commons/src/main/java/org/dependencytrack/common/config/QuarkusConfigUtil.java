package org.dependencytrack.common.config;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Optional;

public final class QuarkusConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusConfigUtil.class);

    private QuarkusConfigUtil() {
    }

    public static <T> Optional<T> getConfigMapping(final Class<T> clazz) {
        try {
            final var config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
            return Optional.of(config.getConfigMapping(clazz));
        } catch (NoSuchElementException | IllegalStateException e) {
            // When running tests without @QuarkusTest, resolving of the ConfigMapping will not work.
            LOGGER.debug("Config mapping of class {} could not be resolved", clazz.getName(), e);
            return Optional.empty();
        }
    }

}
