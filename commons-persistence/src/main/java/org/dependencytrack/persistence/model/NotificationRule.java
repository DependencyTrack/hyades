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
package org.dependencytrack.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
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
import org.dependencytrack.persistence.converter.NotificationLevelConverter;
import org.dependencytrack.persistence.converter.NotificationScopeConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Entity
@Table(name = "NOTIFICATIONRULE")
public class NotificationRule extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ENABLED")
    private boolean enabled;

    @Column(name = "SCOPE", nullable = false, columnDefinition = "varchar")
    @Convert(converter = NotificationScopeConverter.class)
    private NotificationScope scope;

    @Column(name = "NOTIFICATION_LEVEL", columnDefinition = "varchar")
    @Convert(converter = NotificationLevelConverter.class)
    private NotificationLevel notificationLevel;

    @OneToMany
    @JoinTable(
            name = "NOTIFICATIONRULE_PROJECTS",
            joinColumns = @JoinColumn(name = "NOTIFICATIONRULE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "PROJECT_ID", referencedColumnName = "ID")
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

    @Column(name = "NOTIFY_ON")
    private String notifyOn;

    @Column(name = "MESSAGE")
    private String message;

    @JoinColumn(name = "PUBLISHER", columnDefinition = "bigint")
    @ManyToOne(cascade = CascadeType.ALL)
    private NotificationPublisher publisher;

    @Column(name = "PUBLISHER_CONFIG")
    private String publisherConfig;

    @Column(name = "UUID")
    private UUID uuid;

    @Column(name = "NOTIFY_CHILDREN")
    private boolean notifyChildren;


    /**
     * In addition to warnings and errors, also emit a log message upon successful publishing.
     * <p>
     * Intended to aid in debugging of missing notifications, or environments where notification
     * delivery is critical and subject to auditing.
     *
     * @since 4.10.0
     */
    @Column(name = "LOG_SUCCESSFUL_PUBLISH")
    private Boolean logSuccessfulPublish;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogSuccessfulPublish() {
        return logSuccessfulPublish != null ? logSuccessfulPublish : false;
    }

    public void setLogSuccessfulPublish(final boolean logSuccessfulPublish) {
        this.logSuccessfulPublish = logSuccessfulPublish;
    }

    public NotificationScope getScope() {
        return scope;
    }

    public void setScope(NotificationScope scope) {
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
