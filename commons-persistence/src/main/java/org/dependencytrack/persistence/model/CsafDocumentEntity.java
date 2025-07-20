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

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;

/**
 * Model for configured CSAF document entities.
 */
@Entity
@Table(name = "CSAFDOCUMENTENTITY")
public class CsafDocumentEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "URL")
    private String url;

    @Column(name = "CONTENT", columnDefinition = "CLOB")
    @Basic(fetch = FetchType.LAZY)
    private String content;

    @Column(name = "PUBLISHERNAMESPACE")
    private String publisherNamespace;

    @Column(name = "TRACKINGID")
    private String trackingID;

    @Column(name = "TRACKINGVERSION")
    private String trackingVersion;

    @Column(name = "SEEN")
    private boolean seen;

    @Column(name = "MANUALLYADDED")
    private boolean manuallyAdded;

    @Column(name = "LASTFETCHED")
    private Instant lastFetched;

    public CsafDocumentEntity() {
        // no args for jdo
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublisherNamespace() {
        return publisherNamespace;
    }

    public void setPublisherNamespace(String publisherNamespace) {
        this.publisherNamespace = publisherNamespace;
    }

    public String getTrackingID() {
        return trackingID;
    }

    public void setTrackingID(String trackingID) {
        this.trackingID = trackingID;
    }

    public String getTrackingVersion() {
        return trackingVersion;
    }

    public void setTrackingVersion(String trackingVersion) {
        this.trackingVersion = trackingVersion;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isManuallyAdded() {
        return manuallyAdded;
    }

    public Instant getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(Instant lastFetched) {
        this.lastFetched = lastFetched;
    }

}
