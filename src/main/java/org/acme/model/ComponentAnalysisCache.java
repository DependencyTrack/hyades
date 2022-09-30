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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.acme.serde.CustomJsonDeserializer;

import javax.jdo.annotations.*;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.UUID;

/**
 * Model class for tracking when component data was last analyzed. This may be used for vulnerability or outdated
 * component analysis, or any future forms of analysis.
 *
 * @author Steve Springett
 * @since 3.6.0
 */
@PersistenceCapable
@Unique(name="COMPONENTANALYSISCACHE_COMPOSITE_IDX", members={"cacheType", "targetHost", "targetType", "target"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentAnalysisCache implements Serializable {

    private static final long serialVersionUID = 1189261128713368621L;

    public enum CacheType {
        REPOSITORY,
        VULNERABILITY
    }

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @JsonIgnore
    private long id;

    @Persistent
    @Column(name = "LAST_OCCURRENCE", allowsNull = "false")
    @NotNull
    private Date lastOccurrence;

    @Persistent
    @Column(name = "CACHE_TYPE", allowsNull = "false")
    @NotNull
    private CacheType cacheType;

    @Persistent
    @Column(name = "TARGET_HOST", allowsNull = "false")
    @NotNull
    private String targetHost;

    @Persistent
    @Column(name = "TARGET_TYPE", allowsNull = "false")
    @NotNull
    private String targetType;

    @Persistent
    @Column(name = "TARGET", allowsNull = "false")
    @NotNull
    private String target;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "RESULT", allowsNull = "true", jdbcType = "CLOB")
    private String result;

    @Persistent(customValueStrategy = "uuid")
    @Unique(name = "COMPONENTANALYSISCACHE_UUID_IDX")
    @Column(name = "UUID", jdbcType = "VARCHAR", length = 36, allowsNull = "false")
    @NotNull
    private UUID uuid;


    public long getId() {
        return id;
    }

    public void setId(long id) {
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
    //@JsonDeserialize(using = CustomJsonDeserializer.class)
    @JsonGetter
    public String getResult() {

        return result;
    }


   /* public void setResult(JsonObject jsonObject) {
        if (jsonObject == null) {
            result = null;
        } else {
            try (final StringWriter sw = new StringWriter();
                 final JsonWriter jw = Json.createWriter(sw)) {
                jw.write(jsonObject);
                result = sw.toString();
            } catch (Exception e) {
                result = null;
            }
        }
    }*/
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
