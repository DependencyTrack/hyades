package org.hyades.vulnmirror.datasource.github;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.datasource.github")
public interface GitHubConfig {

    Optional<String> baseUrl();

    Optional<String> apiKey();

}
