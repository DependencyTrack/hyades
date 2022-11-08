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
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * The Analysis model tracks human auditing decisions for vulnerabilities found
 * on a given dependency.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@Entity
@Table(name = "ANALYSIS", uniqueConstraints = {@UniqueConstraint(columnNames = {"PROJECT_ID", "COMPONENT_ID", "VULNERABILITY_ID"}, name = "ANALYSIS_COMPOSITE_IDX")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Analysis implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    private long id;

    @Column(name = "PROJECT_ID")
    @JsonIgnore
    private Project project;

    @Column(name = "COMPONENT_ID")
    @JsonIgnore
    private Component component;

    @Column(name = "VULNERABILITY_ID", nullable = false)
    @NotNull
    @JsonIgnore
    private Vulnerability vulnerability;

    @Column(name = "STATE", columnDefinition = "VARCHAR", nullable = false)
    @NotNull
    private AnalysisState analysisState;

    @Column(name = "JUSTIFICATION", columnDefinition = "VARCHAR", nullable = true)
    @NotNull
    private AnalysisJustification analysisJustification;

    @Column(name = "RESPONSE", columnDefinition = "VARCHAR", nullable = true)
    @NotNull
    private AnalysisResponse analysisResponse;

    @Lob
    @Column(name = "DETAILS", columnDefinition = "text", nullable = true)
    @NotNull
    private String analysisDetails;


    @OrderBy("timestamp ASC")
    @OneToMany
    private List<AnalysisComment> analysisComments;

    @Column(name = "SUPPRESSED")
    @JsonProperty(value = "isSuppressed")
    private boolean suppressed;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
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

    public AnalysisState getAnalysisState() {
        return analysisState;
    }

    public void setAnalysisState(AnalysisState analysisState) {
        this.analysisState = analysisState;
    }

    public AnalysisJustification getAnalysisJustification() {
        return analysisJustification;
    }

    public void setAnalysisJustification(AnalysisJustification analysisJustification) {
        this.analysisJustification = analysisJustification;
    }

    public AnalysisResponse getAnalysisResponse() {
        return analysisResponse;
    }

    public void setAnalysisResponse(AnalysisResponse analysisResponse) {
        this.analysisResponse = analysisResponse;
    }

    public String getAnalysisDetails() {
        return analysisDetails;
    }

    public void setAnalysisDetails(String analysisDetails) {
        this.analysisDetails = analysisDetails;
    }

    public List<AnalysisComment> getAnalysisComments() {
        return analysisComments;
    }

    public void setAnalysisComments(List<AnalysisComment> analysisComments) {
        this.analysisComments = analysisComments;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }
}
