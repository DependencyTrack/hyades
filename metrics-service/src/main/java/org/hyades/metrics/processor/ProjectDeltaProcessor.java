package org.hyades.metrics.processor;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hyades.proto.metrics.v1.ProjectMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hyades.metrics.util.MetricsUtil.calculateDelta;
import static org.hyades.metrics.util.MetricsUtil.deleted;
import static org.hyades.proto.metrics.v1.Status.STATUS_CREATED;
import static org.hyades.proto.metrics.v1.VulnerabilityStatus.VULNERABILITY_STATUS_UNKNOWN;
import static org.hyades.proto.metrics.v1.VulnerabilityStatus.VULNERABILITY_STATUS_VULNERABLE;

public class ProjectDeltaProcessor extends ContextualProcessor<String, ProjectMetrics, String, ProjectMetrics> {

    private String storeName;
    private KeyValueStore<String, ProjectMetrics> store;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDeltaProcessor.class);

    public ProjectDeltaProcessor(String storeName) {
        this.storeName = storeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ProcessorContext<String, ProjectMetrics> context) {
        super.init(context);
        store = context().getStateStore(storeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(Record<String, ProjectMetrics> record) {

        String projectUuId = record.key();
        ProjectMetrics projectMetrics = record.value();
        ProjectMetrics lastProjectMetrics = store.get(projectUuId);

        ProjectMetrics deltaProjectMetrics;

        if (record.value() == null) {
            LOGGER.info("Tombstone event for project metrics for project id: {}. Deleting record from store", projectUuId);
            store.delete(projectUuId);
            deltaProjectMetrics = deleted(lastProjectMetrics);
        } else {
            deltaProjectMetrics = lastProjectMetrics == null
                    ? newProjectMetrics(projectMetrics)
                    : calculateDelta(lastProjectMetrics, projectMetrics);
            LOGGER.debug("Forwarding record to sink from delta processor {}", projectUuId);
            store.put(projectUuId, projectMetrics);
        }
        this.context().forward(new Record<>(projectUuId, deltaProjectMetrics, context().currentSystemTimeMs()));
    }

    private static ProjectMetrics newProjectMetrics(ProjectMetrics projectMetrics) {
        return ProjectMetrics.newBuilder(projectMetrics)
                .setStatus(STATUS_CREATED)
                .setVulnerabilityStatus(projectMetrics.getVulnerabilities().getTotal() > 0
                        ? VULNERABILITY_STATUS_VULNERABLE
                        : VULNERABILITY_STATUS_UNKNOWN)
                .build();
    }

}
