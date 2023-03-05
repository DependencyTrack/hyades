package org.hyades.metrics.processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.hyades.metrics.model.ProjectMetrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

@ApplicationScoped
public class ProjectProcessorSupplier implements ProcessorSupplier<String, ProjectMetrics, String, ProjectMetrics> {

    private final StoreBuilder<KeyValueStore<String, ProjectMetrics>> deltaStoreBuilder;

    @Inject
    public ProjectProcessorSupplier(@Named("deltaProjectStoreBuilder") final StoreBuilder<KeyValueStore<String, ProjectMetrics>> deltaStoreBuilder) {
        this.deltaStoreBuilder = deltaStoreBuilder;
    }

    @Override
    public Processor<String, ProjectMetrics, String, ProjectMetrics> get() {
        return new ProjectDeltaProcessor(deltaStoreBuilder.name());
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(deltaStoreBuilder);
    }
}
