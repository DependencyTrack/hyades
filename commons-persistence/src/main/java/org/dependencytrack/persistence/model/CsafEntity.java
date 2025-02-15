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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Model for configured CSAF source Entities.
 */
@Entity
@Table(name = "CSAFENTITY")
public class CsafEntity implements Serializable {

    @Id
    @Column(name = "CSAFENTRYID")
    private long csafEntryId;

    @Column(name = "ENTITYTYPE", columnDefinition = "VARCHAR")
    private String entityType;

    @Column(name = "CONTENT", columnDefinition = "BLOB")
    private byte[] content;

    @Column(name = "URL", columnDefinition = "VARCHAR")
    @NotBlank
    private String url;

    @Column(name = "ENABLED")
    @NotNull
    private boolean enabled;

    public long getCsafEntryId() {
        return csafEntryId;
    }

    public void setCsafEntryId(long csafEntryId) {
        this.csafEntryId = csafEntryId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
