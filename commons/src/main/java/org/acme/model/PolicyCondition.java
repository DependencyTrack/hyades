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
import java.util.UUID;

/**
 * Defines a Model class for defining a policy condition.
 *
 * @author Steve Springett
 * @since 4.0.0
 */

@RegisterForReflection
public class PolicyCondition implements Serializable {

    public enum Operator {
        IS,
        IS_NOT,
        MATCHES,
        NO_MATCH,
        NUMERIC_GREATER_THAN,
        NUMERIC_LESS_THAN,
        NUMERIC_EQUAL,
        NUMERIC_NOT_EQUAL,
        NUMERIC_GREATER_THAN_OR_EQUAL,
        NUMERIC_LESSER_THAN_OR_EQUAL,
        CONTAINS_ALL,
        CONTAINS_ANY
    }

    public enum Subject {
        //AGE,
        //ANALYZER,
        //BOM,
        COORDINATES,
        CPE,
        //INHERITED_RISK_SCORE,
        LICENSE,
        LICENSE_GROUP,
        PACKAGE_URL,
        SEVERITY,
        SWID_TAGID,
        VERSION,
        COMPONENT_HASH,
        CWE
    }

    private long id;

    private Policy policy;

    private Operator operator;

    private Subject subject;

    private String value;

    /**
     * The unique identifier of the object.
     */
    private UUID uuid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
