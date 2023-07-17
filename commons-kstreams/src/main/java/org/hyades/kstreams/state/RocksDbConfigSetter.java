package org.hyades.kstreams.state;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.config.SmallRyeConfig;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.eclipse.microprofile.config.ConfigProvider;
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
        final SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        final StateStoreConfig stateStoreConfig = config.getConfigMapping(StateStoreConfig.class);

        stateStoreConfig.rocksDb().compactionStyle().ifPresent(options::setCompactionStyle);
        stateStoreConfig.rocksDb().compressionType().ifPresent(options::setCompressionType);
    }

    @Override
    public void close(final String storeName, final Options options) {
    }

}
