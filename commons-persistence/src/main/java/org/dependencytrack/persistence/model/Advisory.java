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
 * Model for security advisories (CSAF documents).
 */
@Entity
@Table(name = "ADVISORY")
public class Advisory implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * A machine-readable name of the CSAF document. This is typically the "document.tracking.id" field.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * The version of the CSAF document. This is typically the "document.tracking.version" field.
     */
    @Column(name = "VERSION")
    private String version;

    /**
     * The publisher (namespace) of the CSAF document. This is typically the "document.publisher.namespace" field.
     */
    @Column(name = "PUBLISHER")
    private String publisher;

    /**
     * A human-readable title for the CSAF document.
     */
    @Column(name = "TITLE")
    private String title;

    /**
     * The URL where the CSAF document can be found externally.
     */
    @Column(name = "URL")
    private String url;

    /**
     * The format of the document, e.g., "CSAF".
     */
    @Column(name = "FORMAT")
    private String format;

    /**
     * The raw content of the CSAF document, typically in JSON format.
     */
    @Column(name = "CONTENT", columnDefinition = "CLOB")
    @Basic(fetch = FetchType.LAZY)
    private String content;

    /**
     * Whether the document has been marked as "seen" in the UI.
     */
    @Column(name = "SEEN")
    private boolean seen;

    /**
     * The time when the document was last fetched from the external source.
     */
    @Column(name = "LASTFETCHED")
    private Instant lastFetched;

    public Advisory() {
        // no args for JPA
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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

}
