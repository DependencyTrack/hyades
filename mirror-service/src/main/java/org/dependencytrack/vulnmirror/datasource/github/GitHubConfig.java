package org.dependencytrack.vulnmirror.datasource.github;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.datasource.github")
public interface GitHubConfig {

    Optional<String> baseUrl();

    Optional<String> apiKey();

    @WithDefault("false")
    boolean aliasSyncEnabled();
}
