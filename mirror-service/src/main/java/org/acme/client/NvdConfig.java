package org.acme.client;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.nvd")
public interface NvdConfig {

    ApiConfig api();

    interface ApiConfig {

        Optional<String> apiKey();
    }
}
