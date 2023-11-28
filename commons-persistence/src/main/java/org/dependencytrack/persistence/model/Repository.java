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
import org.dependencytrack.persistence.converter.RepositoryTypeConverter;

import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "REPOSITORY")
public class Repository {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "TYPE", columnDefinition = "VARCHAR")
    @Convert(converter = RepositoryTypeConverter.class)
    private RepositoryType type;

    @Column(name = "IDENTIFIER")
    private String identifier;

    @Column(name = "URL")
    private String url;

    @Column(name = "RESOLUTION_ORDER")
    private int resolutionOrder;

    @Column(name = "ENABLED")
    private boolean enabled;

    @Column(name = "INTERNAL")
    private Boolean internal;

    @Column(name = "AUTHENTICATIONREQUIRED")
    private Boolean authenticationRequired;

    @Column(name = "USERNAME")
    private String username;


    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "UUID")
    private UUID uuid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RepositoryType getType() {
        return type;
    }

    public void setType(RepositoryType type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getResolutionOrder() {
        return resolutionOrder;
    }

    public void setResolutionOrder(int resolutionOrder) {
        this.resolutionOrder = resolutionOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = Optional.ofNullable(internal).orElse(false);
    }

    public Boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public void setAuthenticationRequired(Boolean authenticationRequired) {
        this.authenticationRequired = Optional.ofNullable(authenticationRequired).orElse(false);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}
