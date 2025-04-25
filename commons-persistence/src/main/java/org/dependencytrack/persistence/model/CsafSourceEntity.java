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

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.io.Serializable;
import java.time.Instant;

/**
 * Model for configured CSAF source entities.
 *
 * 
 * @since 5.6.0 //TODO set when merged
 */
@Entity
@Table(name = "CSAFSOURCEENTITY")
public class CsafSourceEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "URL")
    private String url;

    @Column(name = "ENABLED")
    private boolean enabled;

    @Column(name = "CONTENT", columnDefinition = "CLOB")
    private String content;

    @Column(name = "SEEN")
    private boolean seen;

    @Column(name = "LASTFETCHED")
    private Instant lastFetched;

    @Column(name = "AGGREGATOR")
    private boolean aggregator;

    @Column(name = "DISCOVERY")
    private boolean discovery;

    @Column(name = "DOMAIN")
    private boolean domain;

    public CsafSourceEntity() {
        // no args for jdo
    }

    public CsafSourceEntity(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setEntryId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Instant getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(Instant lastFetched) {
        this.lastFetched = lastFetched;
    }

    public boolean isAggregator() {
        return aggregator;
    }

    public void setAggregator(boolean aggregator) {
        this.aggregator = aggregator;
    }

    public boolean isDiscovery() {
        return discovery;
    }

    public void setDiscovery(boolean discovery) {
        this.discovery = discovery;
    }

    public boolean isDomain() {
        return domain;
    }

    public void setDomain(boolean domain) {
        this.domain = domain;
    }
}
