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
package org.dependencytrack.vulnmirror.state;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;

/**
 * A {@link ContextualProcessor} for updating global state stores.
 * <p>
 * Note that this processor must not perform any transformations on the records,
 * it must simply insert or delete records from the store.
 * <p>
 * Refer to {@link StreamsBuilder#addGlobalStore(StoreBuilder, String, Consumed, ProcessorSupplier)} for details.
 *
 * @param <K> Type of the record key
 * @param <V> Type of the record value
 */
public class StateStoreUpdater<K, V> extends ContextualProcessor<K, V, Void, Void> {

    private final String storeName;
    private KeyValueStore<K, V> store;

    public StateStoreUpdater(final String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void init(final ProcessorContext<Void, Void> context) {
        super.init(context);

        store = context().getStateStore(storeName);
    }

    @Override
    public void process(final Record<K, V> record) {
        if (record.value() == null) {
            store.delete(record.key());
        } else {
            store.put(record.key(), record.value());
        }
    }

}
