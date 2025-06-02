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
import org.apache.kafka.streams.state.internals.RocksDBKeyValueBytesStoreSupplier;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(StateStoreUtilWithQuarkusConfigTest.TestProfile.class)
class StateStoreUtilWithQuarkusConfigTest {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "state-store.type", "rocks_db"
            );
        }
    }

    @Inject // Force injection, otherwise Quarkus will not discover the config mapping.
    @SuppressWarnings("unused")
    StateStoreConfig stateStoreConfig;

    @Test
    void test() {
        assertThat(StateStoreUtil.configurableKeyValueStore("storeName"))
                .isInstanceOf(RocksDBKeyValueBytesStoreSupplier.class);
    }

}
