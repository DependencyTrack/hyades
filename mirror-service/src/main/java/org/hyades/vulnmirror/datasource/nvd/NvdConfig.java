package org.hyades.vulnmirror.datasource.nvd;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.datasource.nvd")
public interface NvdConfig {

    Optional<String> baseUrl();

    Optional<String> apiKey();

    int numThreads();

}
