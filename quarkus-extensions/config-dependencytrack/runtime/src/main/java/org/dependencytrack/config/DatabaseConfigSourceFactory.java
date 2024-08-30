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

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalDataSourceConfiguration.DataSourceImplementation;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceContext.ConfigSourceContextConfigSource;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

class DatabaseConfigSourceFactory implements ConfigSourceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfigSourceFactory.class);

    @Override
    public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context) {
        final SmallRyeConfig config = new SmallRyeConfigBuilder()
                .withSources(new ConfigSourceContextConfigSource(context))
                .withProfiles(context.getProfiles())
                .withMapping(DatabaseConfigSourceConfig.class)
                .build();

        final var configSourceConfig = config.getConfigMapping(DatabaseConfigSourceConfig.class);
        if (!configSourceConfig.enabled()) {
            LOGGER.debug("Config source disabled");
            return Collections.emptyList();
        }

        final DataSource dataSource;
        try {
            dataSource = createDataSource(configSourceConfig);
        } catch (SQLException e) {
            LOGGER.warn("Failed to initialize datasource", e);
            return Collections.emptyList();
        }

        return List.of(new DatabaseConfigSource(dataSource, configSourceConfig.cache()));
    }

    private static DataSource createDataSource(final DatabaseConfigSourceConfig config) throws SQLException {
        final var dataSourceConfig = new AgroalDataSourceConfigurationSupplier();
        if (!config.pool().enabled()) {
            dataSourceConfig.dataSourceImplementation(DataSourceImplementation.AGROAL_POOLLESS);
        }

        final AgroalConnectionPoolConfigurationSupplier connectionPoolConfig =
                dataSourceConfig.connectionPoolConfiguration();
        connectionPoolConfig
                .initialSize(config.pool().initialSize())
                .minSize(config.pool().minSize())
                .maxSize(config.pool().maxSize());
        config.acquisitionTimeout()
                .ifPresent(connectionPoolConfig::acquisitionTimeout);
        config.idleRemovalInterval()
                .ifPresent(connectionPoolConfig::reapTimeout);

        final AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfig =
                connectionPoolConfig.connectionFactoryConfiguration();
        connectionFactoryConfig
                .jdbcUrl(config.jdbcUrl());
        config.username()
                .map(NamePrincipal::new)
                .ifPresent(connectionFactoryConfig::credential);
        config.password()
                .map(SimplePassword::new)
                .ifPresent(connectionFactoryConfig::credential);

        return AgroalDataSource.from(dataSourceConfig.get());
    }

}
