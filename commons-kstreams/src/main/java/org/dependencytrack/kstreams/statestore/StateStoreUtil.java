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

import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.dependencytrack.common.config.QuarkusConfigUtil;

import java.util.Map;

import static org.apache.kafka.streams.state.Stores.inMemoryKeyValueStore;
import static org.apache.kafka.streams.state.Stores.persistentKeyValueStore;

public final class StateStoreUtil {

    private StateStoreUtil() {
    }

    /**
     * Provide a {@link KeyValueBytesStoreSupplier} implementation, depending on whether
     * {@link StateStoreConfig#type()} is configured to be {@link Materialized.StoreType#IN_MEMORY}
     * or {@link Materialized.StoreType#ROCKS_DB}.
     *
     * @param name The name of the store
     * @return A {@link KeyValueBytesStoreSupplier}
     */
    public static KeyValueBytesStoreSupplier configurableKeyValueStore(final String name) {
        return QuarkusConfigUtil.getConfigMapping(StateStoreConfig.class)
                .map(StateStoreConfig::type)
                .map(storeType -> switch (storeType) {
                    case IN_MEMORY -> inMemoryKeyValueStore(name);
                    case ROCKS_DB -> persistentKeyValueStore(name);
                })
                .orElseGet(() -> inMemoryKeyValueStore(name));
    }

    /**
     * Provide a default configuration to be used for changelog topics.
     *
     * @return The default topic configuration
     */
    public static Map<String, String> defaultChangelogTopicConfig() {
        return Map.of(
                TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT,
                TopicConfig.SEGMENT_BYTES_CONFIG, String.valueOf(64 * 1024 * 1024), // 64 MiB
                TopicConfig.MAX_COMPACTION_LAG_MS_CONFIG, "0" // Perform compaction ASAP
        );
    }

}
