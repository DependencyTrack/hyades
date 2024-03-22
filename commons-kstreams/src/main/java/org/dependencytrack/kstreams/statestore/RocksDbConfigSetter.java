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
import org.dependencytrack.common.config.QuarkusConfigUtil;
import org.rocksdb.Options;

import java.util.Map;

/**
 * An implementation of {@link RocksDBConfigSetter} for customizing RocksDB.
 * <p>
 * Customizations are configurable via Quarkus Config. Available configuration options are defined in {@link StateStoreConfig.RocksDbConfig}.
 *
 * @see <a href="https://kafka.apache.org/34/documentation/streams/developer-guide/config-streams#rocksdb-config-setter">Kafka Streams Documentation</a>
 */
@RegisterForReflection
public class RocksDbConfigSetter implements RocksDBConfigSetter {

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
        QuarkusConfigUtil.getConfigMapping(StateStoreConfig.class)
                .map(StateStoreConfig::rocksDb)
                .ifPresent(config -> {
                    config.compactionStyle().ifPresent(options::setCompactionStyle);
                    config.compressionType().ifPresent(options::setCompressionType);
                });
    }

    @Override
    public void close(final String storeName, final Options options) {
        // Nothing to close here.
    }

}
