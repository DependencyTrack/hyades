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

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.apache.kafka.streams.kstream.Materialized.StoreType;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;

import java.util.Optional;
import java.util.OptionalDouble;

@ConfigMapping(prefix = "state-store")
public interface StateStoreConfig {

    @WithDefault("in_memory")
    StoreType type();

    RocksDbConfig rocksDb();

    interface RocksDbConfig {

        /**
         * The Kafka Streams default is 50MiB <em>per state store</em>.
         * We use the same cache across all stores, so this should be considered.
         */
        @WithDefault(/* 128MiB */ "134217728")
        long blockCacheSizeBytes();

        /**
         * Ratio of the block cache size that shall be reserved for high priority blocks,
         * such as indexes and filters, preventing them from being evicted.
         */
        OptionalDouble highPriorityPoolRatio();

        @WithDefault(/* 16MiB */ "16777216")
        long writeBufferSize();

        @WithDefault(/* 4KiB */ "4096")
        long blockSizeBytes();

        Optional<CompactionStyle> compactionStyle();

        Optional<CompressionType> compressionType();

    }

}
