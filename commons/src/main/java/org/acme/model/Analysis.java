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

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;
import java.util.List;

/**
 * The Analysis model tracks human auditing decisions for vulnerabilities found
 * on a given dependency.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@RegisterForReflection
public class Analysis implements Serializable {

    private int id;
    private Project project;

    private Component component;

    private Vulnerability vulnerability;

    private AnalysisState analysisState;

    private AnalysisJustification analysisJustification;

    private AnalysisResponse analysisResponse;

    private String analysisDetails;

    private List<AnalysisComment> analysisComments;

    private boolean suppressed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
