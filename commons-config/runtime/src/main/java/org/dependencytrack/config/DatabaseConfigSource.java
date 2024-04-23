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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.config.common.AbstractConfigSource;
import org.dependencytrack.config.DatabaseConfigSourceConfig.CacheConfig;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link ConfigSource} that pulls properties from the {@code CONFIGPROPERTY} database table.
 * <p>
 * The source only supports properties with {@value #PROPERTY_PREFIX} prefix.
 * Properties in the {@code CONFIGPROPERTY} table do not have to have this prefix.
 * <p>
 * If enabled, this config source has a higher priority ({@code 500}) than any of the default
 * sources. This is to facilitate the database as preferred source of truth.
 *
 * @see <a href="https://quarkus.io/guides/config-reference#configuration-sources">Default sources</a>
 */
class DatabaseConfigSource extends AbstractConfigSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfigSource.class);
    private static final String PROPERTY_PREFIX = "dtrack";

    private final DataSource dataSource;
    private final LoadingCache<String, String> cache;
    private final ReentrantLock lock = new ReentrantLock();

    public DatabaseConfigSource(final DataSource dataSource, final CacheConfig cacheConfig) {
        super("database", 500);
        this.dataSource = dataSource;

        if (cacheConfig.enabled()) {
            final Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
            cacheConfig.expireAfterWrite().ifPresent(cacheBuilder::expireAfterWrite);
            this.cache = cacheBuilder.build(new PropertyCacheLoader());
        } else {
            this.cache = null;
        }
    }

    @Override
    public Set<String> getPropertyNames() {
        LOGGER.debug("Loading available property names");

        lock.lock(); // getPropertyNames may be called concurrently.
        final var propertyNames = new HashSet<String>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("""
                     SELECT ("GROUPNAME" || '.' || "PROPERTYNAME")
                     FROM "CONFIGPROPERTY"
                     """);
             final ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                propertyNames.add("%s.%s".formatted(PROPERTY_PREFIX, rs.getString(1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load property names", e);
        } finally {
            lock.unlock();
        }

        LOGGER.debug("Available property names: {}", propertyNames);
        return propertyNames;
    }

    @Override
    public String getValue(final String name) {
        if (cache == null) {
            try {
                return loadProperty(name);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load property %s".formatted(name));
            }
        }

        return cache.get(name);
    }

    private String loadProperty(final String name) throws SQLException {
        // Quarkus will try to load all properties that were not found in other
        // config sources. This can cause significant load on the database due
        // to the sheer amount of properties supported by Quarkus.
        // To reduce the impact, only consider properties prefixed with "dtrack".
        final String[] parts = name.split("\\.", 3);
        if (parts.length != 3 || !PROPERTY_PREFIX.equals(parts[0])) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("""
                        Not loading property {}: Does not match expected format of \
                        {}.<groupName>.<propertyName>""", name, PROPERTY_PREFIX);
            }

            return null;
        }

        LOGGER.debug("Loading property: {}", name);
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("""
                     SELECT "PROPERTYVALUE"
                       FROM "CONFIGPROPERTY"
                      WHERE "GROUPNAME" = ?
                        AND "PROPERTYNAME" = ?
                     """)) {
            ps.setString(1, parts[1]);
            ps.setString(2, parts[2]);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        return null;
    }

    /**
     * Workaround for {@code Cannot determine if CacheLoader can bulk load} warning logs
     * in native executables. Normally the CacheLoader can be a lambda, but then GraalVM
     * optimizes the {@code loadAll} method away that Caffeine uses to determine whether
     * bulk loading is supported.
     *
     * @see <a href="https://stackoverflow.com/a/73033351">Relevant StackOverflow Post</a>
     */
    @RegisterForReflection
    private class PropertyCacheLoader implements CacheLoader<String, String> {

        @Override
        public String load(final String key) throws Exception {
            return loadProperty(key);
        }

    }

}
