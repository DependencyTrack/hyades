package org.dependencytrack.kstreams.statestore;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.apache.kafka.streams.kstream.Materialized.StoreType;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;

import java.util.Optional;

@ConfigMapping(prefix = "state-store")
public interface StateStoreConfig {

    @WithDefault("in_memory")
    StoreType type();

    RocksDbConfig rocksDb();

    interface RocksDbConfig {

        Optional<CompactionStyle> compactionStyle();

        Optional<CompressionType> compressionType();

    }

}
