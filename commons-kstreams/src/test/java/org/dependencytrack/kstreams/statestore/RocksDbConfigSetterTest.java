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
package org.dependencytrack.kstreams.statestore;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Suite
@SelectClasses(value = {
        RocksDbConfigSetterTest.WithoutQuarkusConfigTest.class,
        RocksDbConfigSetterTest.WithQuarkusConfigTest.class
})
class RocksDbConfigSetterTest {

    static class WithoutQuarkusConfigTest {

        @Test
        void testSetConfig() {
            final var rocksDbOptions = new Options();

            final var configSetter = new RocksDbConfigSetter();
            configSetter.setConfig("storeName", rocksDbOptions, Collections.emptyMap());

            assertThat(rocksDbOptions)
                    .usingRecursiveComparison()
                    .ignoringFields("nativeHandle_")
                    .isEqualTo(new Options());
        }

    }

    @QuarkusTest
    @TestProfile(WithQuarkusConfigTest.TestProfile.class)
    static class WithQuarkusConfigTest {

        public static class TestProfile implements QuarkusTestProfile {

            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "state-store.rocks-db.compaction-style", "universal",
                        "state-store.rocks-db.compression-type", "zstd_compression"
                );
            }
        }

        @Inject // Force injection, otherwise Quarkus will not discover the config mapping.
        @SuppressWarnings("unused")
        StateStoreConfig stateStoreConfig;

        @Test
        void testSetConfig() {
            final var rocksDbOptions = new Options();

            final var configSetter = new RocksDbConfigSetter();
            configSetter.setConfig("storeName", rocksDbOptions, Collections.emptyMap());

            assertThat(rocksDbOptions.compactionStyle()).isEqualTo(CompactionStyle.UNIVERSAL);
            assertThat(rocksDbOptions.compressionType()).isEqualTo(CompressionType.ZSTD_COMPRESSION);
        }

    }

}