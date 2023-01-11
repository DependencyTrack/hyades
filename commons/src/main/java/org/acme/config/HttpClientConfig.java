package org.acme.config;

import alpine.Config;
import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "client.http.config")
public interface HttpClientConfig {
    Optional<String> proxyAddress();

    Optional<Integer> proxyPort();

    Optional<String> proxyUsername();

    Optional<String> proxyPassword();
    Optional<String> noProxy();
    int proxyTimeoutConnection();
    int proxyTimeoutPool();
    int proxyTimeoutSocket();

}
