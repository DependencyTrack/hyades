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

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.apache.kafka.streams.state.internals.BlockBasedTableConfigWithAccessibleCache;
import org.dependencytrack.common.config.QuarkusConfigUtil;
import org.dependencytrack.kstreams.statestore.StateStoreConfig.RocksDbConfig;
import org.rocksdb.Cache;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.WriteBufferManager;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An implementation of {@link RocksDBConfigSetter} for customizing RocksDB.
 * <p>
 * Customizations are configurable via Quarkus Config. Available configuration options are defined in {@link RocksDbConfig}.
 *
 * @see <a href="https://kafka.apache.org/34/documentation/streams/developer-guide/config-streams#rocksdb-config-setter">Kafka Streams Documentation</a>
 */
@RegisterForReflection
public class RocksDbConfigSetter implements RocksDBConfigSetter {

    private static Cache BLOCK_CACHE;
    private static WriteBufferManager WRITE_BUFFER_MANAGER;
    private static boolean INITIALIZED = false;
    private static final ReentrantLock INIT_LOCK = new ReentrantLock();

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        final Optional<RocksDbConfig> optionalConfig = QuarkusConfigUtil
                .getConfigMapping(StateStoreConfig.class)
                .map(StateStoreConfig::rocksDb);
        if (optionalConfig.isEmpty()) {
            return;
        }

        final RocksDbConfig config = optionalConfig.get();

        INIT_LOCK.lock();
        try {
            maybeInit(config);
        } finally {
            INIT_LOCK.unlock();
        }

        final var tableConfig = (BlockBasedTableConfigWithAccessibleCache) options.tableFormatConfig();

        // Kafka Streams configures a default cache of size 50MiB for each state store.
        // Be a good citizen and ensure that default cache is closed after overwriting it.
        try (final Cache ignoredDefaultCache = tableConfig.blockCache()) {
            tableConfig.setBlockCache(BLOCK_CACHE);
        }

        // Ensure the memory used by RocksDB is limited to the size of the block cache.
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setBlockSize(config.blockSizeBytes());
        options.setWriteBufferManager(WRITE_BUFFER_MANAGER);

        // If a high priority pool ratio is configured, ensure that RocksDB can make use of that.
        // https://github.com/facebook/rocksdb/wiki/Block-Cache#caching-index-filter-and-compression-dictionary-blocks
        if (config.highPriorityPoolRatio().isPresent()) {
            tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
            tableConfig.setPinTopLevelIndexAndFilter(true);
        }

        config.compactionStyle().ifPresent(options::setCompactionStyle);
        config.compressionType().ifPresent(options::setCompressionType);
    }

    @Override
    public void close(final String storeName, final Options options) {
        // Nothing to close here.
    }

    private void maybeInit(final RocksDbConfig config) {
        if (INITIALIZED) {
            return;
        }

        final OptionalDouble highPriorityPoolRatio = config.highPriorityPoolRatio();
        if (highPriorityPoolRatio.isPresent()) {
            BLOCK_CACHE = new LRUCache(
                    config.blockCacheSizeBytes(),
                    /* numShardBits */ -1,
                    /* strictCapacityLimit */ false,
                    highPriorityPoolRatio.getAsDouble());
        } else {
            BLOCK_CACHE = new LRUCache(config.blockCacheSizeBytes());
        }

        WRITE_BUFFER_MANAGER = new WriteBufferManager(
                config.writeBufferSize(),
                BLOCK_CACHE);

        INITIALIZED = true;
    }

}
