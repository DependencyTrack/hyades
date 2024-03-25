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
package org.dependencytrack.kstreams.processor;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.dependencytrack.kstreams.statestore.StateStoreUtil;

import java.time.Duration;
import java.util.Set;
import java.util.function.Function;

import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;

public class TombstoneEmittingProcessorSupplier<K, V> implements FixedKeyProcessorSupplier<K, V, V> {

    private final StoreBuilder<KeyValueStore<K, Long>> storeBuilder;
    private final Duration checkInterval;
    private final Duration maxLifetime;
    private final Function<K, V> tombstoneSupplier;

    public TombstoneEmittingProcessorSupplier(final String storeName, final Serde<K> keySerde,
                                              final Duration checkInterval, final Duration maxLifetime,
                                              final Function<K, V> tombstoneSupplier) {
        this.storeBuilder = keyValueStoreBuilder(StateStoreUtil.configurableKeyValueStore(storeName), keySerde, Serdes.Long())
                .withLoggingEnabled(StateStoreUtil.defaultChangelogTopicConfig());
        this.checkInterval = checkInterval;
        this.maxLifetime = maxLifetime;
        this.tombstoneSupplier = tombstoneSupplier;
    }

    @Override
    public FixedKeyProcessor<K, V, V> get() {
        return new TombstoneEmittingProcessor<>(storeBuilder.name(), checkInterval, maxLifetime, tombstoneSupplier);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(storeBuilder);
    }
}
