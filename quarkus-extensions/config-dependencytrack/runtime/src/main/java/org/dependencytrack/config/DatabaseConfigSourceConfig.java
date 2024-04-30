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

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.DurationConverter;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Optional;

@ConfigMapping(prefix = "quarkus.config.source.dtrack.database")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
interface DatabaseConfigSourceConfig {

    /**
     * Whether the database configuration source shall be enabled.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The JDBC URL to use for the database connection.
     */
    @WithDefault("${quarkus.datasource.jdbc.url}")
    String jdbcUrl();

    /**
     * The username to use for the database connection.
     */
    @WithDefault("${quarkus.datasource.username}")
    Optional<String> username();

    /**
     * The password to use for the database connection.
     */
    @WithDefault("${quarkus.datasource.password}")
    Optional<String> password();

    /**
     * The timeout for acquiring new database connections.
     */
    @WithDefault("${quarkus.datasource.jdbc.acquisition-timeout}")
    @WithConverter(DurationConverter.class)
    Optional<Duration> acquisitionTimeout();

    /**
     * Connection pooling configuration.
     */
    PoolConfig pool();

    /**
     * Caching configuration.
     */
    CacheConfig cache();

    interface PoolConfig {

        /**
         * Whether to connection pooling shall be enabled.
         */
        @WithDefault("${quarkus.datasource.jdbc.pooling-enabled}")
        boolean enabled();

        /**
         * The initial size of the connection pool.
         */
        @WithDefault("0")
        int initialSize();

        /**
         * The minimum size of the connection pool.
         */
        @WithDefault("0")
        int minSize();

        /**
         * The maximum size of the connection pool.
         */
        @WithDefault("2")
        int maxSize();

    }

    interface CacheConfig {

        /**
         * Whether caching shall be enabled.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * The duration for which properties shall be cached.
         */
        @WithDefault("PT5M")
        @WithConverter(DurationConverter.class)
        Optional<Duration> expireAfterWrite();

    }

}
