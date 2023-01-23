package org.acme.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import static org.apache.kafka.streams.state.Stores.inMemoryKeyValueStore;
import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;

public class NvdClientConfig {

    @Produces
    @ApplicationScoped
    NvdClient nvdClient(@Named("LastModifiedEpochStoreBuilder") StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder,
                          NvdConfig config) {
        return new NvdClient(lastModifiedEpochStoreBuilder, config.api().apiKey().orElse(null));
    }

    @Produces
    @ApplicationScoped
    @Named("nvdObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        return objectMapper.registerModule(new JavaTimeModule());
    }

    @Produces
    @ApplicationScoped
    @Named("LastModifiedEpochStoreBuilder")
    StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder() {
        return keyValueStoreBuilder(inMemoryKeyValueStore("nvd-last-modified-epoch-store"),
                Serdes.String(), Serdes.Long());
    }
}
