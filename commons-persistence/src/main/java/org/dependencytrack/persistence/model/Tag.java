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
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.persistence.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Objects;

/**
 * Model for assigning tags to specific objects.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@Entity
@Table(name = "TAG")
@RegisterForReflection
public class Tag {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "NAME")
    private String name;

    @OrderBy("name ASC")
    @ManyToMany
    @JoinTable(
            name = "PROJECTS_TAGS",
            joinColumns = @JoinColumn(name = "TAG_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID")
    )
    private List<Project> projects;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Tag) {
            return this.id == ((Tag) object).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
