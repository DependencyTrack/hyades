package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;
import java.time.Instant;

import static org.hyades.metrics.util.MetricsUtil.inheritedRiskScore;

@RegisterForReflection
public class PortfolioMetrics extends Counters implements Serializable {

    private int projects;

    private int vulnerableProjects;

    private int components;

    private int vulnerableComponents;


    public PortfolioMetrics add(ProjectMetrics projectMetrics) {
        if (projectMetrics == null) {
            return this;
        }

        this.projects++;
        if (projectMetrics.getVulnerabilities() > 0) {
            this.vulnerableProjects++;
        }

        this.components += projectMetrics.getComponents();
        this.critical += projectMetrics.getCritical();
        this.high += projectMetrics.getHigh();
        this.medium += projectMetrics.getMedium();
        this.low += projectMetrics.getLow();
        this.vulnerabilities += projectMetrics.getVulnerabilities();
        this.vulnerableComponents += projectMetrics.getVulnerableComponents();

        this.findingsAudited += projectMetrics.getFindingsAudited();
        this.findingsTotal += projectMetrics.getFindingsTotal();
        this.findingsUnaudited += projectMetrics.getFindingsUnaudited();
        this.policyViolationsFail += projectMetrics.getPolicyViolationsFail();
        this.policyViolationsInfo += projectMetrics.getPolicyViolationsInfo();
        this.policyViolationsWarn += projectMetrics.getPolicyViolationsWarn();
        this.policyViolationsTotal += projectMetrics.getPolicyViolationsTotal();
        this.policyViolationsAudited += projectMetrics.getPolicyViolationsAudited();
        this.policyViolationsUnaudited += projectMetrics.getPolicyViolationsUnaudited();
        this.policyViolationsLicenseTotal += projectMetrics.getPolicyViolationsLicenseTotal();
        this.policyViolationsLicenseAudited += projectMetrics.getPolicyViolationsLicenseAudited();
        this.policyViolationsLicenseUnaudited += projectMetrics.getPolicyViolationsLicenseUnaudited();
        this.policyViolationsOperationalAudited += projectMetrics.getPolicyViolationsOperationalUnaudited();
        this.policyViolationsOperationalTotal += projectMetrics.getPolicyViolationsOperationalTotal();
        this.policyViolationsOperationalUnaudited += projectMetrics.getPolicyViolationsOperationalUnaudited();
        this.policyViolationsSecurityAudited += projectMetrics.getPolicyViolationsSecurityAudited();
        this.policyViolationsSecurityTotal += projectMetrics.getPolicyViolationsSecurityTotal();
        this.policyViolationsSecurityUnaudited += projectMetrics.getPolicyViolationsSecurityUnaudited();
        this.inheritedRiskScore = inheritedRiskScore(this.critical, this.high, this.medium, this.low, this.unassigned);
        this.lastOccurrence = Instant.now();
        this.firstOccurrence = Instant.now();
        return this;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getProjects() {
        return projects;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public int getVulnerableProjects() {
        return vulnerableProjects;
    }

    public void setVulnerableProjects(int vulnerableProjects) {
        this.vulnerableProjects = vulnerableProjects;
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
}
