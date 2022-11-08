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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

/**
 * Defines a Model class for defining a policy condition.
 *
 * @author Steve Springett
 * @since 4.0.0
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @Id
    @JsonIgnore
    private long id;

    @Column(name = "POLICY_ID", nullable = false)
    private Policy policy;

    @Column(name = "OPERATOR", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The operator may only contain printable characters")
    private Operator operator;

    @Column(name = "SUBJECT", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The subject may only contain printable characters")
    private Subject subject;

    @Column(name = "VALUE", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The value may only contain printable characters")
    private String value;

    /**
     * The unique identifier of the object.
     */
    @Column(name = "UUID", unique = true, length = 36, nullable = false)
    @NotNull
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
