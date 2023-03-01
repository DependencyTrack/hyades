package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.model.Project;

import java.io.Serializable;
import java.util.Date;

import static org.hyades.metrics.model.Status.CREATED;

@RegisterForReflection
public class ProjectMetrics implements Serializable {

    private long id;

    private Project project;

    private int critical;

    private int high;

    private int medium;

    private int low;

    private int unassigned;

    private int vulnerabilities;

    private int vulnerableComponents;

    private int components;

    private int suppressed;

    private int findingsTotal;

    private int findingsAudited;

    private int findingsUnaudited;

    private double inheritedRiskScore;

    private int policyViolationsFail;

    private int policyViolationsWarn;

    private int policyViolationsInfo;

    private int policyViolationsTotal;

    private int policyViolationsAudited;

    private int policyViolationsUnaudited;

    private int policyViolationsSecurityTotal;

    private int policyViolationsSecurityAudited;

    private int policyViolationsSecurityUnaudited;

    private int policyViolationsLicenseTotal;

    private int policyViolationsLicenseAudited;

    private int policyViolationsLicenseUnaudited;

    private int policyViolationsOperationalTotal;

    private int policyViolationsOperationalAudited;

    private int policyViolationsOperationalUnaudited;

    private Date firstOccurrence;

    private Date lastOccurrence;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProjectMetrics add(ComponentMetrics componentMetrics) {
        if (componentMetrics.getStatus().equals(CREATED)) {
            this.project = componentMetrics.getProject();
            this.components++;
        }

        this.vulnerabilities += componentMetrics.getVulnerabilities();
        this.critical += componentMetrics.getCritical();
        this.high += componentMetrics.getHigh();
        this.medium += componentMetrics.getMedium();
        this.low += componentMetrics.getLow();
        this.findingsTotal += componentMetrics.getFindingsTotal();
        this.findingsAudited += componentMetrics.getFindingsAudited();
        this.findingsUnaudited += componentMetrics.getFindingsUnaudited();
        this.policyViolationsAudited += componentMetrics.getPolicyViolationsAudited();
        this.policyViolationsFail += componentMetrics.getPolicyViolationsFail();
        this.policyViolationsInfo += componentMetrics.getPolicyViolationsInfo();
        this.policyViolationsLicenseAudited += componentMetrics.getPolicyViolationsLicenseAudited();
        this.policyViolationsLicenseTotal += componentMetrics.getPolicyViolationsLicenseTotal();
        this.policyViolationsLicenseUnaudited += componentMetrics.getPolicyViolationsLicenseUnaudited();
        this.policyViolationsOperationalAudited += componentMetrics.getPolicyViolationsOperationalAudited();
        this.policyViolationsOperationalUnaudited += componentMetrics.getPolicyViolationsOperationalUnaudited();
        this.policyViolationsOperationalTotal += componentMetrics.getPolicyViolationsOperationalTotal();
        this.policyViolationsSecurityAudited += componentMetrics.getPolicyViolationsSecurityAudited();
        this.policyViolationsSecurityTotal += componentMetrics.getPolicyViolationsSecurityTotal();
        this.policyViolationsSecurityUnaudited += componentMetrics.getPolicyViolationsSecurityUnaudited();

        return this;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(int medium) {
        this.medium = medium;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getUnassigned() {
        return unassigned;
    }

    public void setUnassigned(int unassigned) {
        this.unassigned = unassigned;
    }

    public int getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(int vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public int getVulnerableComponents() {
        return vulnerableComponents;
    }

    public void setVulnerableComponents(int vulnerableComponents) {
        this.vulnerableComponents = vulnerableComponents;
    }

    public int getComponents() {
        return components;
    }

    public void setComponents(int components) {
        this.components = components;
    }

    public int getSuppressed() {
        return suppressed;
    }

    public void setSuppressed(int suppressed) {
        this.suppressed = suppressed;
    }

    public int getFindingsTotal() {
        return findingsTotal;
    }

    public void setFindingsTotal(int findingsTotal) {
        this.findingsTotal = findingsTotal;
    }

    public int getFindingsAudited() {
        return findingsAudited;
    }

    public void setFindingsAudited(int findingsAudited) {
        this.findingsAudited = findingsAudited;
    }

    public int getFindingsUnaudited() {
        return findingsUnaudited;
    }

    public void setFindingsUnaudited(int findingsUnaudited) {
        this.findingsUnaudited = findingsUnaudited;
    }

    public double getInheritedRiskScore() {
        return inheritedRiskScore;
    }

    public void setInheritedRiskScore(double inheritedRiskScore) {
        this.inheritedRiskScore = inheritedRiskScore;
    }

    public int getPolicyViolationsFail() {
        return policyViolationsFail;
    }

    public void setPolicyViolationsFail(int policyViolationsFail) {
        this.policyViolationsFail = policyViolationsFail;
    }

    public int getPolicyViolationsWarn() {
        return policyViolationsWarn;
    }

    public void setPolicyViolationsWarn(int policyViolationsWarn) {
        this.policyViolationsWarn = policyViolationsWarn;
    }

    public int getPolicyViolationsInfo() {
        return policyViolationsInfo;
    }

    public void setPolicyViolationsInfo(int policyViolationsInfo) {
        this.policyViolationsInfo = policyViolationsInfo;
    }

    public int getPolicyViolationsTotal() {
        return policyViolationsTotal;
    }

    public void setPolicyViolationsTotal(int policyViolationsTotal) {
        this.policyViolationsTotal = policyViolationsTotal;
    }

    public int getPolicyViolationsAudited() {
        return policyViolationsAudited;
    }

    public void setPolicyViolationsAudited(int policyViolationsAudited) {
        this.policyViolationsAudited = policyViolationsAudited;
    }

    public int getPolicyViolationsUnaudited() {
        return policyViolationsUnaudited;
    }

    public void setPolicyViolationsUnaudited(int policyViolationsUnaudited) {
        this.policyViolationsUnaudited = policyViolationsUnaudited;
    }

    public int getPolicyViolationsSecurityTotal() {
        return policyViolationsSecurityTotal;
    }

    public void setPolicyViolationsSecurityTotal(int policyViolationsSecurityTotal) {
        this.policyViolationsSecurityTotal = policyViolationsSecurityTotal;
    }

    public int getPolicyViolationsSecurityAudited() {
        return policyViolationsSecurityAudited;
    }

    public void setPolicyViolationsSecurityAudited(int policyViolationsSecurityAudited) {
        this.policyViolationsSecurityAudited = policyViolationsSecurityAudited;
    }

    public int getPolicyViolationsSecurityUnaudited() {
        return policyViolationsSecurityUnaudited;
    }

    public void setPolicyViolationsSecurityUnaudited(int policyViolationsSecurityUnaudited) {
        this.policyViolationsSecurityUnaudited = policyViolationsSecurityUnaudited;
    }

    public int getPolicyViolationsLicenseTotal() {
        return policyViolationsLicenseTotal;
    }

    public void setPolicyViolationsLicenseTotal(int policyViolationsLicenseTotal) {
        this.policyViolationsLicenseTotal = policyViolationsLicenseTotal;
    }

    public int getPolicyViolationsLicenseAudited() {
        return policyViolationsLicenseAudited;
    }

    public void setPolicyViolationsLicenseAudited(int policyViolationsLicenseAudited) {
        this.policyViolationsLicenseAudited = policyViolationsLicenseAudited;
    }

    public int getPolicyViolationsLicenseUnaudited() {
        return policyViolationsLicenseUnaudited;
    }

    public void setPolicyViolationsLicenseUnaudited(int policyViolationsLicenseUnaudited) {
        this.policyViolationsLicenseUnaudited = policyViolationsLicenseUnaudited;
    }

    public int getPolicyViolationsOperationalTotal() {
        return policyViolationsOperationalTotal;
    }

    public void setPolicyViolationsOperationalTotal(int policyViolationsOperationalTotal) {
        this.policyViolationsOperationalTotal = policyViolationsOperationalTotal;
    }

    public int getPolicyViolationsOperationalAudited() {
        return policyViolationsOperationalAudited;
    }

    public void setPolicyViolationsOperationalAudited(int policyViolationsOperationalAudited) {
        this.policyViolationsOperationalAudited = policyViolationsOperationalAudited;
    }

    public int getPolicyViolationsOperationalUnaudited() {
        return policyViolationsOperationalUnaudited;
    }

    public void setPolicyViolationsOperationalUnaudited(int policyViolationsOperationalUnaudited) {
        this.policyViolationsOperationalUnaudited = policyViolationsOperationalUnaudited;
    }

    public Date getFirstOccurrence() {
        return firstOccurrence;
    }

    public void setFirstOccurrence(Date firstOccurrence) {
        this.firstOccurrence = firstOccurrence;
    }

    public Date getLastOccurrence() {
        return lastOccurrence;
    }

    public void setLastOccurrence(Date lastOccurrence) {
        this.lastOccurrence = lastOccurrence;
    }
}
