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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.acme.common.TrimmedStringDeserializer;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Model for tracking individual projects. Projects are high-level containers
 * of components and (optionally) other projects. Project are often software
 * applications, firmware, operating systems, or devices.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@Entity
@Table(name = "PROJECT",indexes = {
        @Index(name = "PROJECT_GROUP_IDX",  columnList="group"),
        @Index(name = "PROJECT_NAME_IDX", columnList="name"),
        @Index(name = "PROJECT_VERSION_IDX", columnList="version"),
        @Index(name = "PROJECT_CLASSIFIER_IDX", columnList="classifier"),
        @Index(name = "PROJECT_CPE_IDX", columnList="cpe"),
        @Index(name = "PROJECT_PURL_IDX", columnList="purl"),
        @Index(name = "PROJECT_SWID_TAGID_IDX", columnList="swidTagId"),
        @Index(name = "PROJECT_LAST_RISKSCORE_IDX", columnList="LAST_RISKSCORE"),
        @Index(name = "PROJECT_LASTBOMIMPORT_FORMAT_IDX", columnList="LAST_BOM_IMPORTED_FORMAT"),
        @Index(name = "PROJECT_LASTBOMIMPORT_IDX", columnList="LAST_BOM_IMPORTED")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"UUID"}, name = "PROJECT_UUID_IDX")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project implements Serializable {

    private static final long serialVersionUID = -7592438796591673355L;

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
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The author may only contain printable characters")
    private String author;


    @Column(name = "PUBLISHER", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The publisher may only contain printable characters")
    private String publisher;

    @Column(name = "GROUP", columnDefinition = "VARCHAR")
    @Size(max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The group may only contain printable characters")
    private String group;

    @Column(name = "NAME", columnDefinition = "VARCHAR", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The name may only contain printable characters")
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "VARCHAR")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The description may only contain printable characters")
    private String description;

    @Column(name = "VERSION", columnDefinition = "VARCHAR")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The version may only contain printable characters")
    private String version;

    @Column(name = "CLASSIFIER", columnDefinition = "VARCHAR")
    //@Extension(vendorName = "datanucleus", key = "enum-check-constraint", value = "true")
    private Classifier classifier;

    @Size(max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    //Patterns obtained from https://csrc.nist.gov/schema/cpe/2.3/cpe-naming_2.3.xsd
    @Pattern(regexp = "(cpe:2\\.3:[aho\\*\\-](:(((\\?*|\\*?)([a-zA-Z0-9\\-\\._]|(\\\\[\\\\\\*\\?!\"#$$%&'\\(\\)\\+,/:;<=>@\\[\\]\\^`\\{\\|}~]))+(\\?*|\\*?))|[\\*\\-])){5}(:(([a-zA-Z]{2,3}(-([a-zA-Z]{2}|[0-9]{3}))?)|[\\*\\-]))(:(((\\?*|\\*?)([a-zA-Z0-9\\-\\._]|(\\\\[\\\\\\*\\?!\"#$$%&'\\(\\)\\+,/:;<=>@\\[\\]\\^`\\{\\|}~]))+(\\?*|\\*?))|[\\*\\-])){4})|([c][pP][eE]:/[AHOaho]?(:[A-Za-z0-9\\._\\-~%]*){0,6})", message = "The CPE must conform to the CPE v2.2 or v2.3 specification defined by NIST")
    private String cpe;

    @Size(max = 255)
    @com.github.packageurl.validator.PackageURL
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String purl;

    @Size(max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The SWID tagId may only contain printable characters")
    private String swidTagId;

    //    @Persistent(table = "PROJECTS_TAGS", defaultFetchGroup = "true", mappedBy = "projects")
//    @Join(column = "PROJECT_ID")
//    @Element(column = "TAG_ID")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "PROJECT",
            joinColumns = @JoinColumn(name = "PROJECT_ID")
    )
    @OrderBy("name ASC")
    private List<Tag> tags;

    @Column(name = "DIRECT_DEPENDENCIES", columnDefinition = "CLOB")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String directDependencies; // This will be a JSON string

    //@Persistent(customValueStrategy = "uuid")
    @Column(name = "UUID", columnDefinition = "VARCHAR", length = 36, nullable = false, unique = true)
    @NotNull
    private UUID uuid;


    @ManyToOne
    @JoinColumn(name = "PARENT_PROJECT_ID", referencedColumnName = "ID")
    private Project parent;

    /**
     * Convenience field which will contain the date of the last entry in the {@link Bom} table
     */
    @Column(name = "LAST_BOM_IMPORTED")
    private Date lastBomImport;

    /**
     * Convenience field which will contain the format of the last entry in the {@link Bom} table
     */
    @Column(name = "LAST_BOM_IMPORTED_FORMAT")
    private String lastBomImportFormat;

    /**
     * Convenience field which stores the Inherited Risk Score (IRS) of the last metric in the ProjectMetrics table
     */
    @Column(name = "LAST_RISKSCORE", nullable = true) // New column, must allow nulls on existing databases))
    private Double lastInheritedRiskScore;

    @Column(name = "ACTIVE")
    @JsonSerialize(nullsUsing = BooleanDefaultTrueSerializer.class)
    private Boolean active; // Added in v3.6. Existing records need to be nullable on upgrade.


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
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public String getCpe() {
        return cpe;
    }

    public void setCpe(String cpe) {
        this.cpe = cpe;
    }

    @JsonSerialize(using = CustomPackageURLSerializer.class)
    public PackageURL getPurl() {
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

    public String getSwidTagId() {
        return swidTagId;
    }

    public void setSwidTagId(String swidTagId) {
        this.swidTagId = swidTagId;
    }

    public String getDirectDependencies() {
        return directDependencies;
    }

    public void setDirectDependencies(String directDependencies) {
        this.directDependencies = directDependencies;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Project getParent() {
        return parent;
    }

    public void setParent(Project parent) {
        this.parent = parent;
    }

    public Date getLastBomImport() {
        return lastBomImport;
    }

    public void setLastBomImport(Date lastBomImport) {
        this.lastBomImport = lastBomImport;
    }

    public String getLastBomImportFormat() {
        return lastBomImportFormat;
    }

    public void setLastBomImportFormat(String lastBomImportFormat) {
        this.lastBomImportFormat = lastBomImportFormat;
    }

    public Double getLastInheritedRiskScore() {
        return lastInheritedRiskScore;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setLastInheritedRiskScore(Double lastInheritedRiskScore) {
        this.lastInheritedRiskScore = lastInheritedRiskScore;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    private final static class BooleanDefaultTrueSerializer extends JsonSerializer<Boolean> {

        @Override
        public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeBoolean(value != null ? value : true);
        }

    }
}
