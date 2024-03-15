package org.dependencytrack.vulnmirror.datasource.epss;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.datasource.epss")
public interface EpssConfig {

    Optional<String> downloadUrl();
}
