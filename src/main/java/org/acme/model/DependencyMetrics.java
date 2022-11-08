/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.acme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Metrics specific for dependencies (project/component relationship).
 *
 * @author Steve Springett
 * @since 3.1.0
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "DEPENDENCYMETRICS",indexes = {
        @Index(name = "DEPENDENCYMETRICS_COMPOSITE_IDX",  columnList="project, component"),
        @Index(name = "DEPENDENCYMETRICS_FIRST_OCCURRENCE_IDX", columnList="firstOccurrence"),
        @Index(name = "DEPENDENCYMETRICS_LAST_OCCURRENCE_IDX", columnList="lastOccurrence")})
public class DependencyMetrics implements Serializable {

    private static final long serialVersionUID = 5231823328085979791L;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    private long id;

    @Column(name = "PROJECT_ID", nullable = false)
    @NotNull
    private Project project;

    @Column(name = "COMPONENT_ID", nullable = false)
    @NotNull
    private Component component;

    @Column(name = "CRITICAL")
    private int critical;

    @Column(name = "HIGH")
    private int high;

    @Column(name = "MEDIUM")
    private int medium;

    @Column(name = "LOW")
    private int low;

    @Column(name = "UNASSIGNED_SEVERITY", nullable = true) // New column, must allow nulls on existing databases)
    private Integer unassigned;

    @Column(name = "VULNERABILITIES")
    private int vulnerabilities;

    @Column(name = "SUPPRESSED")
    private int suppressed;

    @Column(name = "FINDINGS_TOTAL", nullable = true) // New column, must allow nulls on existing databases)
    private Integer findingsTotal;

    @Column(name = "FINDINGS_AUDITED", nullable = true) // New column, must allow nulls on existing databases)
    private Integer findingsAudited;

    @Column(name = "FINDINGS_UNAUDITED", nullable = true) // New column, must allow nulls on existing databases)
    private Integer findingsUnaudited;

    @Column(name = "RISKSCORE")
    private double inheritedRiskScore;

    @Column(name = "POLICYVIOLATIONS_FAIL", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsFail;

    @Column(name = "POLICYVIOLATIONS_WARN", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsWarn;

    @Column(name = "POLICYVIOLATIONS_INFO", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsInfo;

    @Column(name = "POLICYVIOLATIONS_TOTAL", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsTotal;

    @Column(name = "POLICYVIOLATIONS_AUDITED", nullable = true) // New column, must allow nulls on existing databases)
    private Integer policyViolationsAudited;

    @Column(name = "POLICYVIOLATIONS_UNAUDITED", nullable = true) // New column, must allow nulls on existing databases)
    private Integer policyViolationsUnaudited;

    @Column(name = "POLICYVIOLATIONS_SECURITY_TOTAL", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsSecurityTotal;

    @Column(name = "POLICYVIOLATIONS_SECURITY_AUDITED", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsSecurityAudited;

    @Column(name = "POLICYVIOLATIONS_SECURITY_UNAUDITED", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsSecurityUnaudited;

    @Column(name = "POLICYVIOLATIONS_LICENSE_TOTAL", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsLicenseTotal;

    @Column(name = "POLICYVIOLATIONS_LICENSE_AUDITED", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsLicenseAudited;

    @Column(name = "POLICYVIOLATIONS_LICENSE_UNAUDITED", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsLicenseUnaudited;

    @Column(name = "POLICYVIOLATIONS_OPERATIONAL_TOTAL", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsOperationalTotal;

    @Column(name = "POLICYVIOLATIONS_OPERATIONAL_AUDITED", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsOperationalAudited;

    @Column(name = "POLICYVIOLATIONS_OPERATIONAL_UNAUDITED", nullable = true) // New column, must allow nulls on existing data bases)
    private Integer policyViolationsOperationalUnaudited;

    @Column(name = "FIRST_OCCURRENCE", nullable = true)
    @NotNull
    private Date firstOccurrence;

    @Column(name = "LAST_OCCURRENCE", nullable = true)
    @NotNull
    private Date lastOccurrence;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
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

    public long getVulnerabilities() {
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
