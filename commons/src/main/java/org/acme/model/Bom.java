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

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Model class for tracking the importing of bill-of-material documents.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
public class Bom implements Serializable {

    private static final long serialVersionUID = -4378439983100141050L;

    public enum Format {
        CYCLONEDX("CycloneDX", "CycloneDX BOM Standard"),
        SPDX("SPDX", "Software Package Data Exchange");

        private final String formatShortName;
        private final String formatLongName;

        Format(final String formatShortName, final String formatLongName) {
            this.formatShortName = formatShortName;
            this.formatLongName = formatLongName;
        }

        public String getFormatShortName() {
            return formatShortName;
        }

        public String getFormatLongName() {
            return formatLongName;
        }
    }

    private long id;

    private Date imported;

    private String bomFormat;

    private String specVersion;

    private Integer bomVersion;

    private String serialNumber;

    private Project project;

    private UUID uploadToken;

    private UUID uuid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getImported() {
        return imported;
    }

    public void setImported(Date imported) {
        this.imported = imported;
    }

    public String getBomFormat() {
        return bomFormat;
    }

    public void setBomFormat(Format format) {
        this.bomFormat = format.formatShortName;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }

    public Integer getBomVersion() {
        return bomVersion;
    }

    public void setBomVersion(Integer bomVersion) {
        this.bomVersion = bomVersion;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public UUID getUploadToken() {
        return uploadToken;
    }

    public void setUploadToken(UUID uploadToken) {
        this.uploadToken = uploadToken;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
