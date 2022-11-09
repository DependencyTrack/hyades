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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.acme.common.TrimmedStringDeserializer;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Model for tracking Common Weakness Enumerations (CWE).
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "CWE", uniqueConstraints = {@UniqueConstraint(columnNames = {"CWEID"}, name = "CWE_CWEID_IDX")})
public class Cwe implements Serializable {

    private static final long serialVersionUID = -2370075071951574877L;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    private int id;

    @Column(name = "CWEID", nullable = false, unique = true)
    private int cweId;

    @Column(name = "NAME", columnDefinition = "VARCHAR", nullable = false)
    @Size(max = 255)
    @NotNull
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The name may only contain printable characters")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCweId() {
        return cweId;
    }

    public void setCweId(int cweId) {
        this.cweId = cweId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
