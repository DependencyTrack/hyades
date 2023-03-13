package org.hyades.metrics.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.arc.Priority;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.hyades.metrics.model.ComponentMetrics;
import org.hyades.metrics.model.ProjectMetrics;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import static org.apache.kafka.streams.state.Stores.inMemoryKeyValueStore;
import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;
import static org.apache.kafka.streams.state.Stores.persistentKeyValueStore;

public class DeltaProcessorConfigurationInMemory {

    @Alternative
    @Priority(1)
    @ApplicationScoped
    @Named("deltaStoreBuilder")
    StoreBuilder<KeyValueStore<String, ComponentMetrics>> deltaStoreBuilder() {
        return keyValueStoreBuilder(inMemoryKeyValueStore("delta-component-store"),
                new Serdes.StringSerde(), Serdes.serdeFrom(new ObjectMapperSerializer<>(),
                        new ObjectMapperDeserializer<>(new TypeReference<>() {
                        })));
    }

    @Alternative
    @Priority(1)
    @ApplicationScoped
    @Named("deltaProjectStoreBuilder")
    StoreBuilder<KeyValueStore<String, ProjectMetrics>> deltaProjectStoreBuilder() {
        return keyValueStoreBuilder(persistentKeyValueStore("delta-project-store"),
                new Serdes.StringSerde(), Serdes.serdeFrom(new ObjectMapperSerializer<>(),
                        new ObjectMapperDeserializer<>(new TypeReference<>() {
                        })));
    }

    @Alternative
    @Priority(1)
    @ApplicationScoped
    @Named("projectMetricsStoreSupplier")
    KeyValueBytesStoreSupplier projectMetricsStoreSupplier() {
        return Stores.inMemoryKeyValueStore("metrics-store-project-test");
    }

    @Alternative
    @Priority(1)
    @ApplicationScoped
    @Named("portfolioMetricsStoreSupplier")
    KeyValueBytesStoreSupplier portfolioMetricsStoreSupplier() {
        return Stores.inMemoryKeyValueStore("metrics-store-portfolio-test");
    }
}
