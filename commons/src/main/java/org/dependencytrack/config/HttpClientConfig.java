/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
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
