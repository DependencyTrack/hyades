package org.hyades.processor;

import io.quarkus.arc.Priority;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Named;

public class DeltaProcessorConfigurationInMemory {

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
