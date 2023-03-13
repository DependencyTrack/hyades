package org.hyades.metrics.processor;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hyades.proto.metrics.v1.ComponentMetrics;
import org.hyades.proto.metrics.v1.VulnerabilityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hyades.metrics.util.MetricsUtil.calculateDelta;
import static org.hyades.metrics.util.MetricsUtil.deleted;
import static org.hyades.proto.metrics.v1.Status.STATUS_CREATED;

public class ComponentDeltaProcessor extends ContextualProcessor<String, ComponentMetrics, String, ComponentMetrics> {

    private String storeName;
    private KeyValueStore<String, ComponentMetrics> store;

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentDeltaProcessor.class);

    public ComponentDeltaProcessor(String storeName) {
        this.storeName = storeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ProcessorContext<String, ComponentMetrics> context) {
        super.init(context);
        store = context().getStateStore(storeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(Record<String, ComponentMetrics> record) {

        String componentId = record.key();
        ComponentMetrics componentMetrics = record.value();
        ComponentMetrics lastComponentMetrics = store.get(componentId);

        ComponentMetrics deltaComponentMetrics;

        if (record.value() == null) {
            LOGGER.info("Tombstone event for component metrics for component id: {} Deleting record from store", componentId);
            store.delete(componentId);
            deltaComponentMetrics = deleted(lastComponentMetrics);
        } else {
            deltaComponentMetrics = lastComponentMetrics == null
                    ? newComponentMetrics(componentMetrics)
                    : calculateDelta(lastComponentMetrics, componentMetrics);
            LOGGER.debug("Forwarding record to sink from delta processor {}", componentId);
            store.put(componentId, componentMetrics);
        }
        this.context().forward(new Record<>(componentId, deltaComponentMetrics, context().currentSystemTimeMs()));
    }

    private static ComponentMetrics newComponentMetrics(ComponentMetrics componentMetrics) {
        return ComponentMetrics.newBuilder(componentMetrics)
                .setStatus(STATUS_CREATED)
                .setVulnerabilityStatus(componentMetrics.getVulnerabilities().getTotal() > 0
                        ? VulnerabilityStatus.VULNERABILITY_STATUS_VULNERABLE
                        : VulnerabilityStatus.VULNERABILITY_STATUS_UNKNOWN)
                .build();
    }

}

