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
package org.dependencytrack.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.dependencytrack.persistence.converter.PropertyTypeConverter;

@Entity
@Table(name = "CONFIGPROPERTY")
public class ConfigProperty implements IConfigProperty {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "GROUPNAME")
    private String groupName;

    @Column(name = "PROPERTYNAME")
    private String propertyName;

    @Column(name = "PROPERTYVALUE")
    private String propertyValue;

    @Column(name = "PROPERTYTYPE", columnDefinition = "VARCHAR")
    @Convert(converter = PropertyTypeConverter.class)
    private PropertyType propertyType;

    @Column(name = "DESCRIPTION")
    private String description;

    public ConfigProperty() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return this.propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public IConfigProperty.PropertyType getPropertyType() {
        return this.propertyType;
    }

    public void setPropertyType(IConfigProperty.PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
