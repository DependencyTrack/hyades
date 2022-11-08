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
import org.acme.tasks.scanners.AnalyzerIdentity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Model class for tracking the attribution of vulnerability identification.
 *
 * @author Steve Springett
 * @since 4.0.0
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"UUID"}, name = "FINDINGATTRIBUTION_UUID_IDX")},
        indexes = {@Index(name = "FINDINGATTRIBUTION_COMPOUND_IDX",  columnList="component, vulnerability")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FindingAttribution implements Serializable {

    private static final long serialVersionUID = -2609603709255246845L;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    private long id;

    @Column(name = "ATTRIBUTED_ON", nullable = false)
    @NotNull
    private Date attributedOn;

    @Column(name = "ANALYZERIDENTITY", nullable = false)
    private AnalyzerIdentity analyzerIdentity;

    @NotNull
    @ManyToOne //TODO- need to check on relationship
    @JoinColumn(name = "COMPONENT_ID", referencedColumnName = "ID")
    private Component component;

    @NotNull
    @ManyToOne //TODO- need to check on relationship
    @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID")
    private Project project;


    @NotNull
    @ManyToOne //TODO- need to check on relationship
    @JoinColumn(name = "VULNERABILITY_ID", referencedColumnName = "ID")
    private Vulnerability vulnerability;

    @Column(name = "ALT_ID", nullable = true)
    private String alternateIdentifier;

    @Column(name = "REFERENCE_URL", nullable = true)
    private String referenceUrl;

    @Column(name = "UUID", columnDefinition = "VARCHAR", length = 36, nullable = false, unique = true)
    @NotNull
    private UUID uuid;

    public FindingAttribution() {}

    public FindingAttribution(Component component, Vulnerability vulnerability, AnalyzerIdentity analyzerIdentity,
                              String alternateIdentifier, String referenceUrl) {
        this.component = component;
        this.project = component.getProject();
        this.vulnerability = vulnerability;
        this.analyzerIdentity = analyzerIdentity;
        this.attributedOn = new Date();
        this.alternateIdentifier = alternateIdentifier;
        this.referenceUrl = referenceUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getAttributedOn() {
        return attributedOn;
    }

    public void setAttributedOn(Date attributedOn) {
        this.attributedOn = attributedOn;
    }

    public AnalyzerIdentity getAnalyzerIdentity() {
        return analyzerIdentity;
    }

    public void setAnalyzerIdentity(AnalyzerIdentity analyzerIdentity) {
        this.analyzerIdentity = analyzerIdentity;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
        this.project = component.getProject();
    }

    public Vulnerability getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(Vulnerability vulnerability) {
        this.vulnerability = vulnerability;
    }

    public String getAlternateIdentifier() {
        return alternateIdentifier;
    }

    public void setAlternateIdentifier(String alternateIdentifier) {
        this.alternateIdentifier = alternateIdentifier;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public void setReferenceUrl(String referenceUrl) {
        this.referenceUrl = referenceUrl;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
