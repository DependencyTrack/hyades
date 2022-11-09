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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.acme.persistence.CacheTypeConverter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Model class for tracking when component data was last analyzed. This may be used for vulnerability or outdated
 * component analysis, or any future forms of analysis.
 *
 * @author Steve Springett
 * @since 3.6.0
 */
@Entity
@Table(name = "COMPONENTANALYSISCACHE", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"CACHE_TYPE", "TARGET_HOST", "TARGET_TYPE", "TARGET"}, name = "COMPONENTANALYSISCACHE_COMPOSITE_IDX"),
        @UniqueConstraint(columnNames = {"UUID"}, name = "COMPONENTANALYSISCACHE_UUID_IDX")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentAnalysisCache implements Serializable {

    private static final long serialVersionUID = 1189261128713368621L;

    public enum CacheType {
        REPOSITORY,
        VULNERABILITY
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    private int id;

    @Column(name = "LAST_OCCURRENCE", nullable = false)
    @NotNull
    private Date lastOccurrence;

    @Column(name = "CACHE_TYPE", columnDefinition = "varchar",nullable = false)
    @NotNull
    @Convert(converter = CacheTypeConverter.class)
    private CacheType cacheType;

    @Column(name = "TARGET_HOST", nullable = false)
    @NotNull
    private String targetHost;

    @Column(name = "TARGET_TYPE", nullable = false)
    @NotNull
    private String targetType;

    @Column(name = "TARGET", nullable = false)
    @NotNull
    private String target;

    @Column(name = "RESULT", nullable = true, columnDefinition = "varchar")
    private String result;

    @Column(name = "UUID", columnDefinition = "VARCHAR", length = 36, nullable = false)
    @NotNull
    private UUID uuid;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLastOccurrence() {
        return lastOccurrence;
    }

    public void setLastOccurrence(Date lastOccurrence) {
        this.lastOccurrence = lastOccurrence;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ComponentAnalysisCache() {
    }

    @JsonGetter
    public String getResult() {

        return result;
    }

    @JsonSetter
    public void setResult(String jsonObject){
        result = jsonObject;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
