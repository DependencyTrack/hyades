package org.hyades.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.arc.Priority;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.hyades.metrics.model.PortfolioMetrics;
import org.hyades.metrics.model.ProjectMetrics;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.apache.kafka.streams.state.Stores.inMemoryKeyValueStore;
import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;

public class DeltaProcessorConfigurationInMemory {
    @Alternative
    @Priority(1)
    @Named("projectMetricsStoreBuilder")
    @Singleton
    StoreBuilder<KeyValueStore<String, ProjectMetrics>> projectMetricsStoreBuilder() {
        return keyValueStoreBuilder(inMemoryKeyValueStore("project-metrics-store-mem"),
                new Serdes.StringSerde(), Serdes.serdeFrom(new ObjectMapperSerializer<>(),
                        new ObjectMapperDeserializer<>(new TypeReference<>() {
                        })));
    }

    @Alternative
    @Priority(1)
    @Singleton
    @Named("portfolioMetricsStoreBuilder")
    StoreBuilder<KeyValueStore<String, PortfolioMetrics>> portfolioMetricsStoreBuilder() {
        return keyValueStoreBuilder(inMemoryKeyValueStore("portfolio-metrics-store-mem"),
                new Serdes.StringSerde(), Serdes.serdeFrom(new ObjectMapperSerializer<>(),
                        new ObjectMapperDeserializer<>(new TypeReference<>() {
                        })));
    }
}
