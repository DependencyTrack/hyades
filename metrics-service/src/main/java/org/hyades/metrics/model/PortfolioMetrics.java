package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;
import java.util.Date;

@RegisterForReflection
public class PortfolioMetrics implements Serializable {

    private long id;

    private int critical;

    private int high;

    private int medium;

    private int low;

    private int unassigned;

    private int vulnerabilities;

    private int projects;

    private int vulnerableProjects;

    private int components;

    private int vulnerableComponents;

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
        this.policyViolationsAudited += projectMetrics.getPolicyViolationsAudited();
        this.policyViolationsFail += projectMetrics.getPolicyViolationsFail();
        this.policyViolationsInfo += projectMetrics.getPolicyViolationsInfo();
        this.policyViolationsWarn += projectMetrics.getPolicyViolationsWarn();
        this.policyViolationsLicenseTotal += projectMetrics.getPolicyViolationsLicenseTotal();
        this.policyViolationsLicenseAudited += projectMetrics.getPolicyViolationsAudited();
        this.policyViolationsLicenseUnaudited += projectMetrics.getPolicyViolationsLicenseUnaudited();
        this.policyViolationsOperationalAudited += projectMetrics.getPolicyViolationsOperationalUnaudited();
        this.policyViolationsOperationalTotal += projectMetrics.getPolicyViolationsOperationalTotal();
        this.policyViolationsOperationalUnaudited += projectMetrics.getPolicyViolationsOperationalUnaudited();
        this.policyViolationsSecurityAudited += projectMetrics.getPolicyViolationsSecurityAudited();
        this.policyViolationsSecurityTotal += projectMetrics.getPolicyViolationsSecurityTotal();
        this.policyViolationsSecurityUnaudited += projectMetrics.getPolicyViolationsSecurityUnaudited();
        this.inheritedRiskScore = inheritedRiskScore(this.critical, this.high, this.medium, this.low, this.unassigned);
        this.lastOccurrence = new Date();
        this.firstOccurrence = new Date();
        return this;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public static double inheritedRiskScore(final int critical, final int high, final int medium, final int low, final int unassigned) {
        return (double) ((critical * 10) + (high * 5) + (medium * 3) + (low * 1) + (unassigned * 5));
    }
}
