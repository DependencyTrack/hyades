package org.hyades.metrics.processor;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.hyades.metrics.model.ProjectMetrics;
import org.hyades.metrics.model.Status;
import org.hyades.metrics.model.VulnerabilityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hyades.metrics.util.MetricsUtil.hasChanged;

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
            deltaProjectMetrics = deletedProjectMetrics(lastProjectMetrics);
        } else {
            deltaProjectMetrics = lastProjectMetrics == null
                    ? newProjectMetrics(projectMetrics)
                    : calculateDelta(projectMetrics, lastProjectMetrics);
            LOGGER.debug("Forwarding record to sink from delta processor {}", projectUuId);
            store.put(projectUuId, projectMetrics);
        }
        this.context().forward(new Record(projectUuId, deltaProjectMetrics, context().currentSystemTimeMs()));
    }

    private static ProjectMetrics calculateDelta(ProjectMetrics projectEventMetrics, ProjectMetrics inMemoryMetrics) {
        ProjectMetrics deltaProjectMetrics = new ProjectMetrics();
        deltaProjectMetrics.setProject(projectEventMetrics.getProject());
        deltaProjectMetrics.setFirstOccurrence(projectEventMetrics.getFirstOccurrence());
        deltaProjectMetrics.setLastOccurrence(projectEventMetrics.getLastOccurrence());

        if (hasChanged(projectEventMetrics, inMemoryMetrics)) {
            deltaProjectMetrics.setStatus(Status.UPDATED);
        } else {
            deltaProjectMetrics.setStatus(Status.NO_CHANGE);
            return deltaProjectMetrics;
        }

        //When a project is updated it can transition from
        //          vulnerable -> not vulnerable
        //          not_vulnerable -> vulnerable
        //          no change in vulnerability status
        if (projectEventMetrics.getVulnerabilities() > 0 && inMemoryMetrics.getVulnerabilities() == 0) {
            deltaProjectMetrics.setVulnerabilityStatus(VulnerabilityStatus.VULNERABLE);
        } else if (projectEventMetrics.getVulnerabilities() == 0 && inMemoryMetrics.getVulnerabilities() > 0) {
            deltaProjectMetrics.setVulnerabilityStatus(VulnerabilityStatus.NOT_VULNERABLE);
        } else {
            deltaProjectMetrics.setVulnerabilityStatus(VulnerabilityStatus.NO_CHANGE);
        }

        deltaProjectMetrics.setComponents(projectEventMetrics.getComponents() - inMemoryMetrics.getComponents());
        deltaProjectMetrics.setVulnerableComponents(projectEventMetrics.getVulnerableComponents() - inMemoryMetrics.getVulnerableComponents());
        deltaProjectMetrics.setCritical(projectEventMetrics.getCritical() - inMemoryMetrics.getCritical());
        deltaProjectMetrics.setHigh(projectEventMetrics.getHigh() - inMemoryMetrics.getHigh());
        deltaProjectMetrics.setMedium(projectEventMetrics.getMedium() - inMemoryMetrics.getMedium());
        deltaProjectMetrics.setLow(projectEventMetrics.getLow() - inMemoryMetrics.getLow());
        deltaProjectMetrics.setVulnerabilities(projectEventMetrics.getVulnerabilities() - inMemoryMetrics.getVulnerabilities());
        deltaProjectMetrics.setUnassigned(projectEventMetrics.getUnassigned() - inMemoryMetrics.getUnassigned());
        deltaProjectMetrics.setSuppressed(projectEventMetrics.getSuppressed() - inMemoryMetrics.getSuppressed());
        deltaProjectMetrics.setFindingsTotal(projectEventMetrics.getFindingsTotal() - inMemoryMetrics.getFindingsTotal());
        deltaProjectMetrics.setFindingsAudited(projectEventMetrics.getFindingsAudited() - inMemoryMetrics.getFindingsAudited());
        deltaProjectMetrics.setFindingsUnaudited(projectEventMetrics.getFindingsUnaudited() - inMemoryMetrics.getFindingsUnaudited());
        deltaProjectMetrics.setPolicyViolationsAudited(projectEventMetrics.getPolicyViolationsAudited() - inMemoryMetrics.getPolicyViolationsAudited());
        deltaProjectMetrics.setPolicyViolationsUnaudited(projectEventMetrics.getPolicyViolationsUnaudited() - inMemoryMetrics.getPolicyViolationsUnaudited());
        deltaProjectMetrics.setPolicyViolationsFail(projectEventMetrics.getPolicyViolationsFail() - inMemoryMetrics.getPolicyViolationsFail());
        deltaProjectMetrics.setPolicyViolationsInfo(projectEventMetrics.getPolicyViolationsInfo() - inMemoryMetrics.getPolicyViolationsInfo());
        deltaProjectMetrics.setPolicyViolationsTotal(projectEventMetrics.getPolicyViolationsTotal() - inMemoryMetrics.getPolicyViolationsTotal());
        deltaProjectMetrics.setPolicyViolationsWarn(projectEventMetrics.getPolicyViolationsWarn() - inMemoryMetrics.getPolicyViolationsWarn());
        deltaProjectMetrics.setPolicyViolationsLicenseUnaudited(projectEventMetrics.getPolicyViolationsLicenseUnaudited() - inMemoryMetrics.getPolicyViolationsLicenseUnaudited());
        deltaProjectMetrics.setPolicyViolationsLicenseAudited(projectEventMetrics.getPolicyViolationsLicenseAudited() - inMemoryMetrics.getPolicyViolationsLicenseAudited());
        deltaProjectMetrics.setPolicyViolationsLicenseTotal(projectEventMetrics.getPolicyViolationsLicenseTotal() - inMemoryMetrics.getPolicyViolationsLicenseTotal());
        deltaProjectMetrics.setPolicyViolationsOperationalAudited(projectEventMetrics.getPolicyViolationsOperationalAudited() - inMemoryMetrics.getPolicyViolationsOperationalAudited());
        deltaProjectMetrics.setPolicyViolationsOperationalUnaudited(projectEventMetrics.getPolicyViolationsOperationalUnaudited() - inMemoryMetrics.getPolicyViolationsOperationalAudited());
        deltaProjectMetrics.setPolicyViolationsOperationalTotal(projectEventMetrics.getPolicyViolationsOperationalTotal() - inMemoryMetrics.getPolicyViolationsOperationalTotal());
        deltaProjectMetrics.setPolicyViolationsSecurityAudited(projectEventMetrics.getPolicyViolationsSecurityAudited() - inMemoryMetrics.getPolicyViolationsSecurityAudited());
        deltaProjectMetrics.setPolicyViolationsSecurityUnaudited(projectEventMetrics.getPolicyViolationsSecurityUnaudited() - inMemoryMetrics.getPolicyViolationsSecurityUnaudited());
        deltaProjectMetrics.setPolicyViolationsSecurityTotal(projectEventMetrics.getPolicyViolationsSecurityTotal() - inMemoryMetrics.getPolicyViolationsSecurityTotal());
        return deltaProjectMetrics;
    }

    private static ProjectMetrics newProjectMetrics(ProjectMetrics projectMetrics) {
        projectMetrics.setStatus(Status.CREATED);
        //Vulnerable project count should only increase when new project has vulnerabilities
        if (projectMetrics.getVulnerabilities() > 0) {
            projectMetrics.setVulnerabilityStatus(VulnerabilityStatus.VULNERABLE);
        }
        return projectMetrics;
    }

    private static ProjectMetrics deletedProjectMetrics(ProjectMetrics projectMetrics) {
        ProjectMetrics deltaMetrics = new ProjectMetrics();
        deltaMetrics.setStatus(Status.DELETED);
        deltaMetrics.setProject(projectMetrics.getProject());
        deltaMetrics.setComponents(0 - projectMetrics.getComponents());
        deltaMetrics.setVulnerableComponents(0 - projectMetrics.getVulnerableComponents());
        deltaMetrics.setVulnerabilities(0 - projectMetrics.getVulnerabilities());
        deltaMetrics.setUnassigned(0 - projectMetrics.getUnassigned());
        deltaMetrics.setSuppressed(0 - projectMetrics.getSuppressed());
        deltaMetrics.setCritical(0 - projectMetrics.getCritical());
        deltaMetrics.setHigh(0 - projectMetrics.getHigh());
        deltaMetrics.setMedium(0 - projectMetrics.getMedium());
        deltaMetrics.setLow(0 - projectMetrics.getLow());
        deltaMetrics.setFindingsTotal(0 - projectMetrics.getFindingsTotal());
        deltaMetrics.setFindingsAudited(0 - projectMetrics.getFindingsAudited());
        deltaMetrics.setFindingsUnaudited(0 - projectMetrics.getFindingsUnaudited());
        deltaMetrics.setPolicyViolationsAudited(0 - projectMetrics.getPolicyViolationsAudited());
        deltaMetrics.setPolicyViolationsUnaudited(0 - projectMetrics.getPolicyViolationsUnaudited());
        deltaMetrics.setPolicyViolationsFail(0 - projectMetrics.getPolicyViolationsFail());
        deltaMetrics.setPolicyViolationsInfo(0 - projectMetrics.getPolicyViolationsInfo());
        deltaMetrics.setPolicyViolationsTotal(0 - projectMetrics.getPolicyViolationsTotal());
        deltaMetrics.setPolicyViolationsWarn(0 - projectMetrics.getPolicyViolationsWarn());
        deltaMetrics.setPolicyViolationsLicenseUnaudited(0 - projectMetrics.getPolicyViolationsLicenseUnaudited());
        deltaMetrics.setPolicyViolationsLicenseAudited(0 - projectMetrics.getPolicyViolationsLicenseAudited());
        deltaMetrics.setPolicyViolationsLicenseTotal(0 - projectMetrics.getPolicyViolationsLicenseTotal());
        deltaMetrics.setPolicyViolationsOperationalAudited(0 - projectMetrics.getPolicyViolationsOperationalAudited());
        deltaMetrics.setPolicyViolationsOperationalUnaudited(0 - projectMetrics.getPolicyViolationsOperationalAudited());
        deltaMetrics.setPolicyViolationsOperationalTotal(0 - projectMetrics.getPolicyViolationsOperationalTotal());
        deltaMetrics.setPolicyViolationsSecurityAudited(0 - projectMetrics.getPolicyViolationsSecurityAudited());
        deltaMetrics.setPolicyViolationsSecurityUnaudited(0 - projectMetrics.getPolicyViolationsSecurityUnaudited());
        deltaMetrics.setPolicyViolationsSecurityTotal(0 - projectMetrics.getPolicyViolationsSecurityTotal());
        return deltaMetrics;
    }
}
