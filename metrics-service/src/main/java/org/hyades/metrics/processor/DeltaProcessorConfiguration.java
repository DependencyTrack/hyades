package org.hyades.metrics.processor;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.metrics.v1.ComponentMetrics;
import org.hyades.proto.metrics.v1.ProjectMetrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.ws.rs.Produces;

import static org.apache.kafka.streams.state.Stores.keyValueStoreBuilder;
import static org.apache.kafka.streams.state.Stores.persistentKeyValueStore;

public class DeltaProcessorConfiguration {

    @Produces
    @ApplicationScoped
    @Named("deltaStoreBuilder")
    StoreBuilder<KeyValueStore<String, ComponentMetrics>> deltaStoreBuilder() {
        return keyValueStoreBuilder(persistentKeyValueStore("delta-component-store"),
                new Serdes.StringSerde(), new KafkaProtobufSerde<>(ComponentMetrics.parser()));
    }

    @Produces
    @ApplicationScoped
    @Named("deltaProjectStoreBuilder")
    StoreBuilder<KeyValueStore<String, ProjectMetrics>> deltaProjectStoreBuilder() {
        return keyValueStoreBuilder(persistentKeyValueStore("delta-project-store"),
                new Serdes.StringSerde(), new KafkaProtobufSerde<>(ProjectMetrics.parser()));
    }

    @Produces
    @ApplicationScoped
    @Named("projectMetricsStoreSupplier")
    KeyValueBytesStoreSupplier projectMetricsStoreSupplier() {
        return Stores.persistentKeyValueStore("project-metrics-store");
    }

    @Produces
    @ApplicationScoped
    @Named("portfolioMetricsStoreSupplier")
    KeyValueBytesStoreSupplier portfolioMetricsStoreSupplier() {
        return Stores.persistentKeyValueStore("portfolio-metrics-store");
    }

}

