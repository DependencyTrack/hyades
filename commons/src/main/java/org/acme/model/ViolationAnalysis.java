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

import java.io.Serializable;
import java.util.List;

/**
 * The ViolationAnalysis model tracks human auditing decisions for policy violations.
 *
 * @author Steve Springett
 * @since 4.0.0
 */
public class ViolationAnalysis implements Serializable {

    private long id;

    private Project project;

    private Component component;

    private PolicyViolation policyViolation;

    private ViolationAnalysisState analysisState;
    private List<ViolationAnalysisComment> analysisComments;

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

    public PolicyViolation getPolicyViolation() {
        return policyViolation;
    }

    public void setPolicyViolation(PolicyViolation policyViolation) {
        this.policyViolation = policyViolation;
    }

    public ViolationAnalysisState getAnalysisState() {
        return analysisState;
    }

    public void setViolationAnalysisState(ViolationAnalysisState analysisState) {
        this.analysisState = analysisState;
    }

    public List<ViolationAnalysisComment> getAnalysisComments() {
        return analysisComments;
    }

    public void setAnalysisComments(List<ViolationAnalysisComment> analysisComments) {
        this.analysisComments = analysisComments;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }
}
