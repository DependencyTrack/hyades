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

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.DataSource;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@QuarkusTestResource(H2DatabaseTestResource.class)
class DatabaseConfigSourceTest extends AbstractDatabaseConfigSourceTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource(
                            "database-config-source/application-default.properties",
                            "application.properties"
                    ));

    @Inject
    DatabaseConfigSourceTest(final DataSource dataSource) {
        super(dataSource);
    }

    @AfterEach
    void afterEach() throws Exception {
        deleteAllProperties();
    }

    @Test
    void testProperty() throws Exception {
        final Config config = ConfigProvider.getConfig();

        assertThat(config.getOptionalValue("dtrack.foo.bar", String.class)).isEmpty();

        createProperty("foo", "bar", "baz");
        assertThat(config.getOptionalValue("dtrack.foo.bar", String.class)).contains("baz");
        assertThat(config.getOptionalValue("foo.bar", String.class)).isEmpty();
    }

    @Test
    void testCaching() throws Exception {
        final Config config = ConfigProvider.getConfig();

        createProperty("foo", "bar", "baz");
        assertThat(config.getOptionalValue("dtrack.foo.bar", String.class)).contains("baz");

        // Delete property from database. Because it's cached, accessing it should still yield it's old value.
        deleteProperty("foo", "bar");
        assertThat(config.getOptionalValue("dtrack.foo.bar", String.class)).contains("baz");

        // Wait for the cache to expire.
        await("Cache Expiration")
                .atMost(Duration.ofSeconds(5))
                .until(() -> config.getOptionalValue("dtrack.foo.bar", String.class).isEmpty());
    }


}
