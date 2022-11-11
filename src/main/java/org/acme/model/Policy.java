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

import alpine.common.validation.RegexSequence;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Defines a Model class for defining a policy.
 *
 * @author Steve Springett
 * @since 4.0.0
 */
public class Policy implements Serializable {

    public enum Operator {
        ALL,
        ANY
    }

    public enum ViolationState {
        INFO,
        WARN,
        FAIL
    }

    private int id;

    /**
     * The String representation of the policy name.
     */
    private String name;

    /**
     * The operator to use when evaluating conditions.
     */
    private Operator operator;

    /**
     * The state the policy should trigger upon violation.
     */
    private ViolationState violationState;

    /**
     * A list of zero-to-n policy conditions.
     */
    private List<PolicyCondition> policyConditions;

    /**
     * A list of zero-to-n projects
     */
//    @Persistent(table = "POLICY_PROJECTS", defaultFetchGroup = "true")
//    @Join(column = "POLICY_ID")
//    @Element(column = "PROJECT_ID")
//    @OrderBy("name ASC, version ASC")
//    private List<Project> projects;

    /**
     * A list of zero-to-n tags
     */
//    @Persistent(table = "POLICY_TAGS", defaultFetchGroup = "true")
//    @Join(column = "POLICY_ID")
//    @Element(column = "TAG_ID")
//    @OrderBy("name ASC")
//    private List<Tag> tags;

    /**
     * The unique identifier of the object.
     */
    private UUID uuid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public ViolationState getViolationState() {
        return violationState;
    }

    public void setViolationState(ViolationState violationState) {
        this.violationState = violationState;
    }

    public List<PolicyCondition> getPolicyConditions() {
        return policyConditions;
    }

    public void setPolicyConditions(List<PolicyCondition> policyConditions) {
        this.policyConditions = policyConditions;
    }

    public void addPolicyCondition(PolicyCondition policyCondition) {
        if (this.policyConditions == null) {
            this.policyConditions = new ArrayList<>();
        }
        this.policyConditions.add(policyCondition);
    }

//    public List<Project> getProjects() {
//        return projects;
//    }
//
//    public void setProjects(List<Project> projects) {
//        this.projects = projects;
//    }
//
//    public boolean isGlobal() {
//        return (projects == null || projects.size() == 0) && (tags == null || tags.size() == 0);
//    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
