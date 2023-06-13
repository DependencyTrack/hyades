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
package org.hyades.persistence.model;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.persistence.converter.ClassifierToStringConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "PROJECT")
@RegisterForReflection
public class Project implements Serializable {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "AUTHOR", columnDefinition = "VARCHAR")
    private String author;

    @Column(name = "PUBLISHER", columnDefinition = "VARCHAR")
    private String publisher;

    @Column(name = "\"GROUP\"", columnDefinition = "VARCHAR")
    private String group;

    @Column(name = "NAME", columnDefinition = "VARCHAR")
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "VARCHAR")
    private String description;

    @Column(name = "VERSION", columnDefinition = "VARCHAR")
    private String version;
    @ManyToOne
    @JoinColumn(name = "PARENT_PROJECT_ID")
    private Project parent;

    @OneToMany(mappedBy = "parent")
    private Collection<Project> children;

    @Column(name = "CLASSIFIER", columnDefinition = "VARCHAR")
    @Convert(converter = ClassifierToStringConverter.class)
    private Classifier classifier;

    @Column(name = "CPE")
    private String cpe;

    @Column(name = "PURL")
    private String purl;

    @Column(name = "SWIDTAGID")
    private String swidTagId;

    @OneToMany
    @JoinTable(
            name = "PROJECTS_TAGS",
            joinColumns = @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID", referencedColumnName = "ID")
    )
    @OrderBy("name ASC")
    private List<Tag> tags;

    @Column(name = "DIRECT_DEPENDENCIES", columnDefinition = "varchar")
    private String directDependencies;

    @Column(name = "UUID")
    private UUID uuid;

    /**
     * Convenience field which will contain the date of the last entry in the BOM table
     */
    @Column(name = "LAST_BOM_IMPORTED")
    private Date lastBomImport;

    /**
     * Convenience field which will contain the format of the last entry in the BOM table
     */
    @Column(name = "LAST_BOM_IMPORTED_FORMAT")
    private String lastBomImportFormat;

    /**
     * Convenience field which stores the Inherited Risk Score (IRS) of the last metric in the ProjectMetrics table
     */
    @Column(name = "LAST_RISKSCORE")
    private Double lastInheritedRiskScore;

    @Column(name = "ACTIVE")
    private Boolean active;


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

    public Collection<Project> getChildren() {
        return children;
    }

    public void setChildren(Collection<Project> children) {
        this.children = children;
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
