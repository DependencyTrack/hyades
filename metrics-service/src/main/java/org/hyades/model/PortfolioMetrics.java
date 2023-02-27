package org.hyades.model;

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

    private Integer unassigned;

    private int vulnerabilities;

    private int projects;

    private int vulnerableProjects;

    private int components;

    private int vulnerableComponents;

    private int suppressed;

    private Integer findingsTotal;

    private Integer findingsAudited;

    private Integer findingsUnaudited;

    private double inheritedRiskScore;

    private Integer policyViolationsFail;

    private Integer policyViolationsWarn;

    private Integer policyViolationsInfo;

    private Integer policyViolationsTotal;

    private Integer policyViolationsAudited;

    private Integer policyViolationsUnaudited;

    private Integer policyViolationsSecurityTotal;

    private Integer policyViolationsSecurityAudited;

    private Integer policyViolationsSecurityUnaudited;

    private Integer policyViolationsLicenseTotal;

    private Integer policyViolationsLicenseAudited;

    private Integer policyViolationsLicenseUnaudited;

    private Integer policyViolationsOperationalTotal;

    private Integer policyViolationsOperationalAudited;

    private Integer policyViolationsOperationalUnaudited;

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

    public static double inheritedRiskScore(final int critical, final int high, final int medium, final int low, final int unassigned) {
        return (double) ((critical * 10) + (high * 5) + (medium * 3) + (low * 1) + (unassigned * 5));
    }
}
