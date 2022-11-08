package org.acme.model;

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

import alpine.common.validation.RegexSequence;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.google.gson.JsonObject;
import org.acme.common.TrimmedStringDeserializer;
import org.apache.commons.lang3.StringUtils;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "COMPONENT", indexes = {
        @Index(name = "COMPONENT_GROUP_IDX",  columnList="group"),
        @Index(name = "COMPONENT_NAME_IDX", columnList="name"),
        @Index(name = "COMPONENT_CPE_IDX", columnList="cpe"),
        @Index(name = "COMPONENT_PURL_IDX", columnList="purl"),
        @Index(name = "COMPONENT_PURL_COORDINATES_IDX", columnList="purlCoordinates"),
        @Index(name = "COMPONENT_SWID_TAGID_IDX", columnList="swidTagId"),
        @Index(name = "COMPONENT_PROJECT_ID_IDX", columnList="PROJECT_ID")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"UUID"}, name = "COMPONENT_UUID_IDX")})
public class Component implements Serializable {

    private static final long serialVersionUID = 6841650046433674702L;

    /**
     * Defines JDO fetch groups for this class.
     */
    public enum FetchGroup {
        ALL
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    private long id;

    @Column(name = "AUTHOR", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The author may only contain printable characters")
    private String author;

    @Column(name = "PUBLISHER", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The publisher may only contain printable characters")
    private String publisher;

    @Column(name = "GROUP", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The group may only contain printable characters")
    private String group;

    @Column(name = "NAME", columnDefinition = "VARCHAR", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The name may only contain printable characters")
    private String name;

    @Column(name = "VERSION", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The version may only contain printable characters")
    private String version;

    @Column(name = "FILENAME", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.FS_DIRECTORY_NAME, message = "The specified filename is not valid and cannot be used as a filename")
    private String filename;

    @Size(max = 255)
    //Patterns obtained from https://csrc.nist.gov/schema/cpe/2.3/cpe-naming_2.3.xsd
    @Pattern(regexp = "(cpe:2\\.3:[aho\\*\\-](:(((\\?*|\\*?)([a-zA-Z0-9\\-\\._]|(\\\\[\\\\\\*\\?!\"#$$%&'\\(\\)\\+,/:;<=>@\\[\\]\\^`\\{\\|}~]))+(\\?*|\\*?))|[\\*\\-])){5}(:(([a-zA-Z]{2,3}(-([a-zA-Z]{2}|[0-9]{3}))?)|[\\*\\-]))(:(((\\?*|\\*?)([a-zA-Z0-9\\-\\._]|(\\\\[\\\\\\*\\?!\"#$$%&'\\(\\)\\+,/:;<=>@\\[\\]\\^`\\{\\|}~]))+(\\?*|\\*?))|[\\*\\-])){4})|([c][pP][eE]:/[AHOaho]?(:[A-Za-z0-9\\._\\-~%]*){0,6})", message = "The CPE must conform to the CPE v2.2 or v2.3 specification defined by NIST")
    private String cpe;

    @Size(max = 255)
    @com.github.packageurl.validator.PackageURL
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String purl;

    @Size(max = 255)
    @com.github.packageurl.validator.PackageURL
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String purlCoordinates; // Field should contain only type, namespace, name, and version. Everything up to the qualifiers

    @Size(max = 255)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The SWID tagId may only contain printable characters")
    private String swidTagId;

    @Column(name = "INTERNAL", nullable = true)
    @JsonProperty("isInternal")
    private Boolean internal;

    @Column(name = "DESCRIPTION", columnDefinition = "VARCHAR", length = 1024)
    @Size(max = 1024)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The description may only contain printable characters")
    private String description;

    @Column(name = "COPYRIGHT", columnDefinition = "VARCHAR", length = 1024)
    @Size(max = 1024)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The copyright may only contain printable characters")
    private String copyright;

    @Column(name = "LICENSE", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The license may only contain printable characters")
    private String license;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID", nullable = false)
    private Project project;

    @Column(name = "UUID", columnDefinition = "VARCHAR", length = 36, nullable = false, unique = true)
    @NotNull
    private UUID uuid;

    private transient String bomRef;
    private transient int usedBy;

    @JsonIgnore
    private transient JsonObject cacheResult;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
