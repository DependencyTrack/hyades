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

import alpine.common.validation.RegexSequence;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.acme.common.TrimmedStringDeserializer;
import org.acme.commonnotification.NotificationGroup;
import org.acme.commonnotification.NotificationScope;
import org.acme.persistence.NotificationLevelConverter;
import org.acme.persistence.NotificationScopeConverter;
import org.acme.persistence.UUIDConverter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Defines a Model class for notification configurations.
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "NOTIFICATIONRULE")
public class NotificationRule extends PanacheEntityBase {

    private static final long serialVersionUID = 2534439091019367263L;

    @Id
    @JsonIgnore
    @Column(name = "ID")
    private int id;

    /**
     * The String representation of the name of the notification.
     */
    @Column(name = "NAME", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The name may only contain printable characters")
    private String name;

    @Column(name = "ENABLED")
    private boolean enabled;

    @Column(name = "SCOPE", nullable = false, columnDefinition = "varchar")
    @NotNull
    @Convert(converter = NotificationScopeConverter.class)
    private NotificationScope scope;

    @Column(name = "NOTIFICATION_LEVEL", columnDefinition = "varchar")
    @Convert(converter = NotificationLevelConverter.class)
    private NotificationLevel notificationLevel;

    //    @Join(column = "NOTIFICATIONRULE_ID")
//    @Element(column = "PROJECT_ID")
//    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "name ASC, version ASC"))
    @OneToMany()
    @JoinTable(
            name = "NOTIFICATIONRULE_PROJECTS",
            joinColumns =
            @JoinColumn(name = "NOTIFICATIONRULE_ID", referencedColumnName = "ID"),
            inverseJoinColumns =
            @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID")
    )
    @OrderBy("name ASC, version ASC")
    private List<Project> projects;

//    @Join(column = "NOTIFICATIONRULE_ID")
//    @Element(column = "TEAM_ID")
//    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "name ASC"))
//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinTable(
//            name = "Team",
//            joinColumns = @JoinColumn(name = "TEAM_ID")
//    )
//    @OrderBy("name ASC")
//    private List<Team> teams;

    @Column(name = "NOTIFY_ON", length = 1024)
    private String notifyOn;

    @Column(name = "MESSAGE", length = 1024)
    @Size(max = 1024)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    @Pattern(regexp = RegexSequence.Definition.PRINTABLE_CHARS, message = "The message may only contain printable characters")
    private String message;

    @JoinColumn(name = "PUBLISHER", columnDefinition = "bigint")
    @ManyToOne(cascade = CascadeType.ALL)
    private NotificationPublisher publisher;

    @Column(name = "PUBLISHER_CONFIG")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String publisherConfig;

    @Column(name = "UUID", length = 36, nullable = false, unique = true)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    private UUID uuid;

    @Column(name = "NOTIFY_CHILDREN") // New column, must allow nulls on existing data bases
    private boolean notifyChildren;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @NotNull
    public NotificationScope getScope() {
        return scope;
    }

    public void setScope(@NotNull NotificationScope scope) {
        this.scope = scope;
    }

    public NotificationLevel getNotificationLevel() {
        return notificationLevel;
    }

    public void setNotificationLevel(NotificationLevel notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    public boolean isNotifyChildren() {
        return notifyChildren;
    }

    public void setNotifyChildren(boolean notifyChildren) {
        this.notifyChildren = notifyChildren;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

//    public List<Team> getTeams() {
//        return teams;
//    }
//
//    public void setTeams(List<Team> teams) {
//        this.teams = teams;
//    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<NotificationGroup> getNotifyOn() {
        Set<NotificationGroup> result = new TreeSet<>();
        if (notifyOn != null) {
            String[] groups = notifyOn.split(",");
            for (String s : groups) {
                result.add(NotificationGroup.valueOf(s.trim()));
            }
        }
        return result;
    }

    public void setNotifyOn(Set<NotificationGroup> groups) {
        if (groups.isEmpty()) {
            this.notifyOn = null;
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<NotificationGroup> list = new ArrayList<>(groups);
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i + 1 < list.size()) {
                sb.append(",");
            }
        }
        this.notifyOn = sb.toString();
    }

    public NotificationPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    public String getPublisherConfig() {
        return publisherConfig;
    }

    public void setPublisherConfig(String publisherConfig) {
        this.publisherConfig = publisherConfig;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
    }
}
