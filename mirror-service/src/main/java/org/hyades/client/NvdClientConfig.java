package org.hyades.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.NvdCveApiBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;

import static org.apache.kafka.streams.state.Stores.inMemoryKeyValueStore;
import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;

public class NvdClientConfig {

    @Produces
    @ApplicationScoped
    NvdClient nvdClient(@Named("LastModifiedEpochStoreBuilder") StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder,
                          NvdConfig config,
                        @Named("NvdCveApiSupplier") BiFunction<String, Long, NvdCveApi> cveApiSupplier) {
        return new NvdClient(lastModifiedEpochStoreBuilder, config.api().apiKey().orElse(null), cveApiSupplier);
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
    public StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder() {
        return keyValueStoreBuilder(inMemoryKeyValueStore("nvd-last-modified-epoch-store"),
                Serdes.String(), Serdes.Long());
    }

    @Produces
    @ApplicationScoped
    @Named("NvdCveApiSupplier")
    private final BiFunction<String, Long, NvdCveApi> cveApiSupplier = (apiKey, lastModified) -> {
        NvdCveApiBuilder builder = NvdCveApiBuilder.aNvdCveApi();
        if (lastModified > 0) {
            var start = ZonedDateTime.from(LocalDateTime.ofEpochSecond(lastModified, 0, ZoneOffset.UTC));
            var end = start.minusDays(-120);
            builder.withLastModifiedFilter(start, end);
        }
        if (apiKey != null) {
            builder.withApiKey(apiKey);
        }
        builder.withThreadCount(4);
        return builder.build();
    };
}
