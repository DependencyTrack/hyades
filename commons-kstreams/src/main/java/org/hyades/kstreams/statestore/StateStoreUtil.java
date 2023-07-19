package org.hyades.kstreams.statestore;

import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.hyades.common.config.QuarkusConfigUtil;

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
