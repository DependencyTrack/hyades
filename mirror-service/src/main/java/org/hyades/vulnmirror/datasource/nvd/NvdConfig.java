package org.hyades.vulnmirror.datasource.nvd;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.datasource.nvd")
public interface NvdConfig {

    Optional<String> baseUrl();

    Optional<String> apiKey();

    int numThreads();

    @WithDefault("2")
    int retryBackoffInitialDurationSeconds();

    @WithDefault("4")
    int retryBackoffMultiplier();

    @WithDefault("64")
    int retryMaxDuration();

    @WithDefault("3")
    int retryMaxAttempts();

}
