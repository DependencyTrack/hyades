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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The VulnerableSoftware is a model class for representing vulnerable software
 * as defined by CPE. In essence, it's a CPE which is directly associated to a
 * vulnerability through the NVD CVE data feeds.
 *
 * @author Steve Springett
 * @since 3.6.0
 */
public class VulnerableSoftware implements ICpe, Serializable {

    private static final long serialVersionUID = -3987946408457131098L;

    private long id;

    private String purl;

    private String purlType;

    private String purlNamespace;

    private String purlName;

    private String purlVersion;

    private String purlQualifiers;

    private String purlSubpath;

    private String cpe22;

    private String cpe23;

    private String part;

    private String vendor;

    private String product;

    private String version;

    private String update;

    private String edition;

    private String language;

    private String swEdition;

    private String targetSw;

    private String targetHw;

    private String other;

    private String versionEndExcluding;

    private String versionEndIncluding;

    private String versionStartExcluding;

    private String versionStartIncluding;

    private boolean vulnerable;

    private List<Vulnerability> vulnerabilities;

    private UUID uuid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPurl() {
        return purl;
    }

    public void setPurl(String purl) {
        this.purl = purl;
    }

    public String getPurlType() {
        return purlType;
    }

    public void setPurlType(String purlType) {
        this.purlType = purlType;
    }

    public String getPurlNamespace() {
        return purlNamespace;
    }

    public void setPurlNamespace(String purlNamespace) {
        this.purlNamespace = purlNamespace;
    }

    public String getPurlName() {
        return purlName;
    }

    public void setPurlName(String purlName) {
        this.purlName = purlName;
    }

    public String getPurlVersion() {
        return purlVersion;
    }

    public void setPurlVersion(String purlVersion) {
        this.purlVersion = purlVersion;
    }

    public String getPurlQualifiers() {
        return purlQualifiers;
    }

    public void setPurlQualifiers(String purlQualifiers) {
        this.purlQualifiers = purlQualifiers;
    }

    public String getPurlSubpath() {
        return purlSubpath;
    }

    public void setPurlSubpath(String purlSubpath) {
        this.purlSubpath = purlSubpath;
    }

    public String getCpe22() {
        return cpe22;
    }

    public void setCpe22(String cpe22) {
        this.cpe22 = cpe22;
    }

    public String getCpe23() {
        return cpe23;
    }

    public void setCpe23(String cpe23) {
        this.cpe23 = cpe23;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSwEdition() {
        return swEdition;
    }

    public void setSwEdition(String swEdition) {
        this.swEdition = swEdition;
    }

    public String getTargetSw() {
        return targetSw;
    }

    public void setTargetSw(String targetSw) {
        this.targetSw = targetSw;
    }

    public String getTargetHw() {
        return targetHw;
    }

    public void setTargetHw(String targetHw) {
        this.targetHw = targetHw;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getVersionEndExcluding() {
        return versionEndExcluding;
    }

    public void setVersionEndExcluding(String versionEndExcluding) {
        this.versionEndExcluding = versionEndExcluding;
    }

    public String getVersionEndIncluding() {
        return versionEndIncluding;
    }

    public void setVersionEndIncluding(String versionEndIncluding) {
        this.versionEndIncluding = versionEndIncluding;
    }

    public String getVersionStartExcluding() {
        return versionStartExcluding;
    }

    public void setVersionStartExcluding(String versionStartExcluding) {
        this.versionStartExcluding = versionStartExcluding;
    }

    public String getVersionStartIncluding() {
        return versionStartIncluding;
    }

    public void setVersionStartIncluding(String versionStartIncluding) {
        this.versionStartIncluding = versionStartIncluding;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public void addVulnerability(Vulnerability vulnerability) {
        if (this.vulnerabilities == null) {
            this.vulnerabilities = new ArrayList<>();
        }
        this.vulnerabilities.add(vulnerability);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
