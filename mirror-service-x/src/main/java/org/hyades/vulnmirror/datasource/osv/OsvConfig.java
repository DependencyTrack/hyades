package org.hyades.vulnmirror.datasource.osv;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.osv")
public interface OsvConfig {

    @WithDefault("https://osv-vulnerabilities.storage.googleapis.com")
    Optional<String> baseurl();

}
