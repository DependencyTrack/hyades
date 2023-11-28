package org.dependencytrack.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

@ConfigMapping(prefix = "client.http.config")
public interface HttpClientConfig {
    Optional<String> proxyAddress();

    Optional<Integer> proxyPort();

    Optional<String> proxyUsername();

    Optional<String> proxyPassword();

    Optional<String> noProxy();

    /**
     * @return Maximum number of seconds to wait for remote connections to be established.
     */
    @WithDefault("3")
    int proxyTimeoutConnection();

    /**
     * @return Maximum number of seconds to wait for a connection from the connection pool.
     */
    @WithDefault("3")
    int proxyTimeoutPool();

    /**
     * @return Maximum number of seconds to wait for data to be returned after a connection was established.
     */
    @WithDefault("3")
    int proxyTimeoutSocket();

    @WithDefault("200")
    int maxTotalConnections();

    @WithDefault("20")
    int maxDefaultConnectionsPerRoute();

    @WithDefault("true")
    boolean isConnectionManagerShared();

}
