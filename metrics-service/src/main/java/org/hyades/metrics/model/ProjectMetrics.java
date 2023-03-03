package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.model.Project;

import java.io.Serializable;
import java.time.Instant;

import static org.hyades.metrics.model.Status.CREATED;
import static org.hyades.metrics.model.Status.DELETED;
import static org.hyades.metrics.model.Status.NO_CHANGE;
import static org.hyades.metrics.model.Status.UNKNOWN;
import static org.hyades.metrics.model.Status.UPDATED;
import static org.hyades.metrics.util.MetricsUtil.inheritedRiskScore;

@RegisterForReflection
public class ProjectMetrics extends Counters implements Serializable {

    private Project project;

    private int components;

    private int vulnerableComponents;

    private Status status = UNKNOWN;


    public ProjectMetrics add(ComponentMetrics componentMetrics) {

        if (componentMetrics.getStatus().equals(CREATED)) {
            this.project = componentMetrics.getProject();
            this.components++;
        }

        if (componentMetrics.getStatus().equals(DELETED)) {
            this.components--;
        }

        if (componentMetrics.getVulnerabilities() > 0) {
            this.vulnerableComponents++;
        }

        if (componentMetrics.getVulnerabilities() < 0) {
            this.vulnerableComponents--;
        }

        if (componentMetrics.getStatus().equals(CREATED)
                || componentMetrics.getStatus().equals(UPDATED)
                || componentMetrics.getStatus().equals(DELETED)) {
            this.status = UPDATED;
        } else {
            this.status = NO_CHANGE;
        }

        this.vulnerabilities += componentMetrics.getVulnerabilities();
        this.critical += componentMetrics.getCritical();
        this.high += componentMetrics.getHigh();
        this.medium += componentMetrics.getMedium();
        this.low += componentMetrics.getLow();
        this.findingsTotal += componentMetrics.getFindingsTotal();
        this.findingsAudited += componentMetrics.getFindingsAudited();
        this.findingsUnaudited += componentMetrics.getFindingsUnaudited();
        this.policyViolationsFail += componentMetrics.getPolicyViolationsFail();
        this.policyViolationsWarn += componentMetrics.getPolicyViolationsWarn();
        this.policyViolationsInfo += componentMetrics.getPolicyViolationsInfo();
        this.policyViolationsTotal += componentMetrics.getPolicyViolationsTotal();
        this.policyViolationsUnaudited += componentMetrics.getPolicyViolationsUnaudited();
        this.policyViolationsAudited += componentMetrics.getPolicyViolationsAudited();
        this.policyViolationsLicenseAudited += componentMetrics.getPolicyViolationsLicenseAudited();
        this.policyViolationsLicenseTotal += componentMetrics.getPolicyViolationsLicenseTotal();
        this.policyViolationsLicenseUnaudited += componentMetrics.getPolicyViolationsLicenseUnaudited();
        this.policyViolationsOperationalAudited += componentMetrics.getPolicyViolationsOperationalAudited();
        this.policyViolationsOperationalUnaudited += componentMetrics.getPolicyViolationsOperationalUnaudited();
        this.policyViolationsOperationalTotal += componentMetrics.getPolicyViolationsOperationalTotal();
        this.policyViolationsSecurityAudited += componentMetrics.getPolicyViolationsSecurityAudited();
        this.policyViolationsSecurityTotal += componentMetrics.getPolicyViolationsSecurityTotal();
        this.policyViolationsSecurityUnaudited += componentMetrics.getPolicyViolationsSecurityUnaudited();
        this.inheritedRiskScore = inheritedRiskScore(this.critical, this.high, this.medium, this.low, this.unassigned);
        this.firstOccurrence = Instant.now();
        this.lastOccurrence = Instant.now();
        return this;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public int getComponents() {
        return components;
    }

    public void setComponents(int components) {
        this.components = components;
    }

    public int getVulnerableComponents() {
        return vulnerableComponents;
    }

    public void setVulnerableComponents(int vulnerableComponents) {
        this.vulnerableComponents = vulnerableComponents;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
