package org.hyades.metrics.processor;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hyades.metrics.model.ComponentMetrics;
import org.hyades.metrics.model.Status;
import org.hyades.metrics.model.VulnerabilityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hyades.metrics.util.MetricsUtil.hasChanged;

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
            deltaComponentMetrics = deletedComponentMetrics(lastComponentMetrics);
        } else {
            deltaComponentMetrics = lastComponentMetrics == null
                    ? newComponentMetrics(componentMetrics)
                    : calculateDelta(componentMetrics, lastComponentMetrics);
            LOGGER.debug("Forwarding record to sink from delta processor {}", componentId);
            store.put(componentId, componentMetrics);
        }
        this.context().forward(new Record(componentId, deltaComponentMetrics, context().currentSystemTimeMs()));
    }

    private static ComponentMetrics calculateDelta(ComponentMetrics componentEventMetrics, ComponentMetrics inMemoryMetrics) {
        ComponentMetrics deltaComponentMetrics = new ComponentMetrics();

        deltaComponentMetrics.setComponent(componentEventMetrics.getComponent());
        deltaComponentMetrics.setProject(componentEventMetrics.getProject());
        deltaComponentMetrics.setFirstOccurrence(componentEventMetrics.getFirstOccurrence());
        deltaComponentMetrics.setLastOccurrence(componentEventMetrics.getLastOccurrence());

        if (hasChanged(componentEventMetrics, inMemoryMetrics)) {
            deltaComponentMetrics.setStatus(Status.UPDATED);
        } else {
            deltaComponentMetrics.setStatus(Status.NO_CHANGE);
            deltaComponentMetrics.setVulnerabilityStatus(VulnerabilityStatus.NO_CHANGE);
            return deltaComponentMetrics;
        }

        if (componentEventMetrics.getVulnerabilities() > 0 && inMemoryMetrics.getVulnerabilities() == 0) {
            deltaComponentMetrics.setVulnerabilityStatus(VulnerabilityStatus.VULNERABLE);
        } else if (componentEventMetrics.getVulnerabilities() == 0 && inMemoryMetrics.getVulnerabilities() > 0) {
            deltaComponentMetrics.setVulnerabilityStatus(VulnerabilityStatus.NOT_VULNERABLE);
        } else {
            deltaComponentMetrics.setVulnerabilityStatus(VulnerabilityStatus.NO_CHANGE);
        }

        deltaComponentMetrics.setCritical(componentEventMetrics.getCritical() - inMemoryMetrics.getCritical());
        deltaComponentMetrics.setHigh(componentEventMetrics.getHigh() - inMemoryMetrics.getHigh());
        deltaComponentMetrics.setMedium(componentEventMetrics.getMedium() - inMemoryMetrics.getMedium());
        deltaComponentMetrics.setLow(componentEventMetrics.getLow() - inMemoryMetrics.getLow());
        deltaComponentMetrics.setFindingsTotal(componentEventMetrics.getFindingsTotal() - inMemoryMetrics.getFindingsTotal());
        deltaComponentMetrics.setFindingsAudited(componentEventMetrics.getFindingsAudited() - inMemoryMetrics.getFindingsAudited());
        deltaComponentMetrics.setFindingsUnaudited(componentEventMetrics.getFindingsUnaudited() - inMemoryMetrics.getFindingsUnaudited());
        deltaComponentMetrics.setPolicyViolationsAudited(componentEventMetrics.getPolicyViolationsAudited() - inMemoryMetrics.getPolicyViolationsAudited());
        deltaComponentMetrics.setPolicyViolationsUnaudited(componentEventMetrics.getPolicyViolationsUnaudited() - inMemoryMetrics.getPolicyViolationsUnaudited());
        deltaComponentMetrics.setPolicyViolationsFail(componentEventMetrics.getPolicyViolationsFail() - inMemoryMetrics.getPolicyViolationsFail());
        deltaComponentMetrics.setPolicyViolationsInfo(componentEventMetrics.getPolicyViolationsInfo() - inMemoryMetrics.getPolicyViolationsInfo());
        deltaComponentMetrics.setPolicyViolationsTotal(componentEventMetrics.getPolicyViolationsTotal() - inMemoryMetrics.getPolicyViolationsTotal());
        deltaComponentMetrics.setPolicyViolationsWarn(componentEventMetrics.getPolicyViolationsWarn() - inMemoryMetrics.getPolicyViolationsWarn());
        deltaComponentMetrics.setPolicyViolationsLicenseUnaudited(componentEventMetrics.getPolicyViolationsLicenseUnaudited() - inMemoryMetrics.getPolicyViolationsLicenseUnaudited());
        deltaComponentMetrics.setPolicyViolationsLicenseAudited(componentEventMetrics.getPolicyViolationsLicenseAudited() - inMemoryMetrics.getPolicyViolationsLicenseAudited());
        deltaComponentMetrics.setPolicyViolationsLicenseTotal(componentEventMetrics.getPolicyViolationsLicenseTotal() - inMemoryMetrics.getPolicyViolationsLicenseTotal());
        deltaComponentMetrics.setPolicyViolationsOperationalAudited(componentEventMetrics.getPolicyViolationsOperationalAudited() - inMemoryMetrics.getPolicyViolationsOperationalAudited());
        deltaComponentMetrics.setPolicyViolationsOperationalUnaudited(componentEventMetrics.getPolicyViolationsOperationalUnaudited() - inMemoryMetrics.getPolicyViolationsOperationalAudited());
        deltaComponentMetrics.setPolicyViolationsOperationalTotal(componentEventMetrics.getPolicyViolationsOperationalTotal() - inMemoryMetrics.getPolicyViolationsOperationalTotal());
        deltaComponentMetrics.setVulnerabilities(componentEventMetrics.getVulnerabilities() - inMemoryMetrics.getVulnerabilities());
        deltaComponentMetrics.setUnassigned(componentEventMetrics.getUnassigned() - inMemoryMetrics.getUnassigned());
        deltaComponentMetrics.setSuppressed(componentEventMetrics.getSuppressed() - inMemoryMetrics.getSuppressed());
        deltaComponentMetrics.setPolicyViolationsSecurityAudited(componentEventMetrics.getPolicyViolationsSecurityAudited() - inMemoryMetrics.getPolicyViolationsSecurityAudited());
        deltaComponentMetrics.setPolicyViolationsSecurityUnaudited(componentEventMetrics.getPolicyViolationsSecurityUnaudited() - inMemoryMetrics.getPolicyViolationsSecurityUnaudited());
        deltaComponentMetrics.setPolicyViolationsSecurityTotal(componentEventMetrics.getPolicyViolationsSecurityTotal() - inMemoryMetrics.getPolicyViolationsSecurityTotal());
        return deltaComponentMetrics;
    }

    private static ComponentMetrics newComponentMetrics(ComponentMetrics componentMetrics) {
        componentMetrics.setStatus(Status.CREATED);
        if (componentMetrics.getVulnerabilities() > 0) {
            componentMetrics.setVulnerabilityStatus(VulnerabilityStatus.VULNERABLE);
        }
        return componentMetrics;
    }

    private static ComponentMetrics deletedComponentMetrics(ComponentMetrics componentMetrics) {
        ComponentMetrics deltaMetrics = new ComponentMetrics();
        deltaMetrics.setStatus(Status.DELETED);
        deltaMetrics.setProject(componentMetrics.getProject());
        deltaMetrics.setComponent(componentMetrics.getComponent());
        deltaMetrics.setCritical(0 - componentMetrics.getCritical());
        deltaMetrics.setHigh(0 - componentMetrics.getHigh());
        deltaMetrics.setMedium(0 - componentMetrics.getMedium());
        deltaMetrics.setLow(0 - componentMetrics.getLow());
        deltaMetrics.setVulnerabilities(0 - componentMetrics.getVulnerabilities());
        deltaMetrics.setUnassigned(0 - componentMetrics.getUnassigned());
        deltaMetrics.setSuppressed(0 - componentMetrics.getSuppressed());
        deltaMetrics.setFindingsTotal(0 - componentMetrics.getFindingsTotal());
        deltaMetrics.setFindingsAudited(0 - componentMetrics.getFindingsAudited());
        deltaMetrics.setFindingsUnaudited(0 - componentMetrics.getFindingsUnaudited());
        deltaMetrics.setPolicyViolationsAudited(0 - componentMetrics.getPolicyViolationsAudited());
        deltaMetrics.setPolicyViolationsUnaudited(0 - componentMetrics.getPolicyViolationsUnaudited());
        deltaMetrics.setPolicyViolationsFail(0 - componentMetrics.getPolicyViolationsFail());
        deltaMetrics.setPolicyViolationsInfo(0 - componentMetrics.getPolicyViolationsInfo());
        deltaMetrics.setPolicyViolationsTotal(0 - componentMetrics.getPolicyViolationsTotal());
        deltaMetrics.setPolicyViolationsWarn(0 - componentMetrics.getPolicyViolationsWarn());
        deltaMetrics.setPolicyViolationsLicenseUnaudited(0 - componentMetrics.getPolicyViolationsLicenseUnaudited());
        deltaMetrics.setPolicyViolationsLicenseAudited(0 - componentMetrics.getPolicyViolationsLicenseAudited());
        deltaMetrics.setPolicyViolationsLicenseTotal(0 - componentMetrics.getPolicyViolationsLicenseTotal());
        deltaMetrics.setPolicyViolationsOperationalAudited(0 - componentMetrics.getPolicyViolationsOperationalAudited());
        deltaMetrics.setPolicyViolationsOperationalUnaudited(0 - componentMetrics.getPolicyViolationsOperationalAudited());
        deltaMetrics.setPolicyViolationsOperationalTotal(0 - componentMetrics.getPolicyViolationsOperationalTotal());
        deltaMetrics.setPolicyViolationsSecurityAudited(0 - componentMetrics.getPolicyViolationsSecurityAudited());
        deltaMetrics.setPolicyViolationsSecurityUnaudited(0 - componentMetrics.getPolicyViolationsSecurityUnaudited());
        deltaMetrics.setPolicyViolationsSecurityTotal(0 - componentMetrics.getPolicyViolationsSecurityTotal());
        return deltaMetrics;
    }
}

