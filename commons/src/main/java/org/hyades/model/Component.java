package org.hyades.model;

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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.common.TrimmedStringDeserializer;
import org.hyades.persistence.UUIDConverter;
import org.apache.commons.lang3.StringUtils;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

/**
 * Model class for tracking individual components.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@Entity
@RegisterForReflection
@Table(name = "COMPONENT")
public class Component extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = 6841650046433674702L;

    /**
     * Defines JDO fetch groups for this class.
     */
    public enum FetchGroup {
        ALL
    }


    @Id
    @Column(name = "ID")
    private int id;

    @Column(name = "AUTHOR", columnDefinition = "VARCHAR")
    private String author;

    @Column(name = "PUBLISHER", columnDefinition = "VARCHAR")
    private String publisher;

    @Column(name = "\"GROUP\"", columnDefinition = "VARCHAR")
    private String group;

    @Column(name = "NAME", columnDefinition = "VARCHAR", nullable = false)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String name;

    @Column(name = "VERSION", columnDefinition = "VARCHAR")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String version;

    @Column(name = "FILENAME", columnDefinition = "VARCHAR")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String filename;

    @Column(name = "CPE")
    private String cpe;

    @Column(name = "PURL")
    @com.github.packageurl.validator.PackageURL
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String purl;

    @Column(name = "PURLCOORDINATES")
    @com.github.packageurl.validator.PackageURL
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String purlCoordinates; // Field should contain only type, namespace, name, and version. Everything up to the qualifiers

    @Column(name = "SWIDTAGID")
    private String swidTagId;

    @Column(name = "INTERNAL", nullable = true)
    @JsonProperty("isInternal")
    private Boolean internal;

    @Column(name = "DESCRIPTION", columnDefinition = "VARCHAR", length = 1024)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String description;

    @Column(name = "COPYRIGHT", columnDefinition = "VARCHAR", length = 1024)
    @Size(max = 1024)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String copyright;

    @Column(name = "LICENSE", columnDefinition = "VARCHAR")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String license;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Project project;

    @Column(name = "UUID", length = 36, nullable = false, unique = true)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    private UUID uuid;

    @Column(name = "MD5", columnDefinition = "VARCHAR", length = 32)
    private String md5;

    @Column(name = "SHA1", columnDefinition = "VARCHAR", length = 40)
    private String sha1;

    @Column(name = "SHA_256", columnDefinition = "VARCHAR", length = 64)
    private String sha256;

    @Column(name = "SHA_512", columnDefinition = "VARCHAR", length = 128)
    private String sha512;

    private transient String bomRef;
    private transient int usedBy;

    @JsonIgnore
    private transient JsonObject cacheResult;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = StringUtils.abbreviate(group, 255);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.abbreviate(name, 255);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = StringUtils.abbreviate(version, 255);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = StringUtils.abbreviate(filename, 255);
    }

    public String getCpe() {
        return cpe;
    }

    public void setCpe(String cpe) {
        this.cpe = StringUtils.abbreviate(cpe, 255);
    }

    @JsonSerialize(using = CustomPackageURLSerializer.class)
    public PackageURL getPurl() {
        if (purl == null) {
            return null;
        }
        try {
            return new PackageURL(purl);
        } catch (MalformedPackageURLException e) {
            return null;
        }
    }

    public void setPurl(PackageURL purl) {
        if (purl != null) {
            this.purl = purl.canonicalize();
        } else {
            this.purl = null;
        }
    }

    public void setPurl(String purl) {
        this.purl = purl;
    }

    @JsonSerialize(using = CustomPackageURLSerializer.class)
    public PackageURL getPurlCoordinates() {
        if (purlCoordinates == null) {
            return null;
        }
        try {
            return new PackageURL(purlCoordinates);
        } catch (MalformedPackageURLException e) {
            return null;
        }
    }

    public void setPurlCoordinates(PackageURL purlCoordinates) {
        if (purlCoordinates != null) {
            this.purlCoordinates = purlCoordinates.canonicalize();
        } else {
            this.purlCoordinates = null;
        }
    }

    public void setPurlCoordinates(String purlCoordinates) {
        this.purlCoordinates = purlCoordinates;
    }

    public String getSwidTagId() {
        return swidTagId;
    }

    public void setSwidTagId(String swidTagId) {
        this.swidTagId = swidTagId;
    }

    public boolean isInternal() {
        if (internal == null) {
            return false;
        }
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getSha512() {
        return sha512;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.abbreviate(description, 1024);
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = StringUtils.abbreviate(copyright, 1024);
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = StringUtils.abbreviate(license, 255);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getBomRef() {
        return bomRef;
    }

    public void setBomRef(String bomRef) {
        this.bomRef = bomRef;
    }

    public int getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(int usedBy) {
        this.usedBy = usedBy;
    }

    public JsonObject getCacheResult() {
        return cacheResult;
    }

    public void setCacheResult(JsonObject cacheResult) {
        this.cacheResult = cacheResult;
    }

    @Override
    public String toString() {
        if (getPurl() != null) {
            return getPurl().canonicalize();
        } else {
            StringBuilder sb = new StringBuilder();
            if (getGroup() != null) {
                sb.append(getGroup()).append(" : ");
            }
            sb.append(getName());
            if (getVersion() != null) {
                sb.append(" : ").append(getVersion());
            }
            return sb.toString();
        }
    }
}
