package org.hyades.vulnmirror.datasource.github;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "mirror.datasource.github")
public interface GitHubConfig {

    String apiKey();

}
