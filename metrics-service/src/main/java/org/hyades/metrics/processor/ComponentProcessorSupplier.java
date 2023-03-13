package org.hyades.metrics.processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.hyades.proto.metrics.v1.ComponentMetrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

@ApplicationScoped
public class ComponentProcessorSupplier implements ProcessorSupplier<String, ComponentMetrics, String, ComponentMetrics> {

    private final StoreBuilder<KeyValueStore<String, ComponentMetrics>> deltaStoreBuilder;

    @Inject
    public ComponentProcessorSupplier(@Named("deltaStoreBuilder") final StoreBuilder<KeyValueStore<String, ComponentMetrics>> deltaStoreBuilder) {
        this.deltaStoreBuilder = deltaStoreBuilder;
    }

    @Override
    public Processor<String, ComponentMetrics, String, ComponentMetrics> get() {
        return new ComponentDeltaProcessor(deltaStoreBuilder.name());
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(deltaStoreBuilder);
    }
}
