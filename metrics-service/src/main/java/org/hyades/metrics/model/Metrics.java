package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;
import java.time.Instant;

@RegisterForReflection
public abstract class Metrics implements Serializable {

    protected long id;

    protected int critical;

    protected int high;

    protected int medium;

    protected int low;

    protected int unassigned;

    protected int vulnerabilities;

    protected int suppressed;

    protected int findingsTotal;

    protected int findingsAudited;

    protected int findingsUnaudited;

    protected double inheritedRiskScore;

    protected int policyViolationsFail;

    protected int policyViolationsWarn;

    protected int policyViolationsInfo;

    protected int policyViolationsTotal;

    protected int policyViolationsAudited;

    protected int policyViolationsUnaudited;

    protected int policyViolationsSecurityTotal;

    protected int policyViolationsSecurityAudited;

    protected int policyViolationsSecurityUnaudited;

    protected int policyViolationsLicenseTotal;

    protected int policyViolationsLicenseAudited;

    protected int policyViolationsLicenseUnaudited;

    protected int policyViolationsOperationalTotal;

    protected int policyViolationsOperationalAudited;

    protected int policyViolationsOperationalUnaudited;

    protected Instant firstOccurrence;

    protected Instant lastOccurrence;

    protected Status status = Status.UNKNOWN;

    private VulnerabilityStatus vulnerabilityStatus = VulnerabilityStatus.UNKNOWN;

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

    public Instant getFirstOccurrence() {
        return firstOccurrence;
    }

    public void setFirstOccurrence(Instant firstOccurrence) {
        this.firstOccurrence = firstOccurrence;
    }

    public Instant getLastOccurrence() {
        return lastOccurrence;
    }

    public void setLastOccurrence(Instant lastOccurrence) {
        this.lastOccurrence = lastOccurrence;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public VulnerabilityStatus getVulnerabilityStatus() {
        return vulnerabilityStatus;
    }

    public void setVulnerabilityStatus(VulnerabilityStatus vulnerabilityStatus) {
        this.vulnerabilityStatus = vulnerabilityStatus;
    }
}
