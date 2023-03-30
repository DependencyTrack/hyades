package org.hyades.vulnmirror.datasource.osv;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "mirror.osv")
public interface OsvConfig {

    Optional<String> baseurl();

}
