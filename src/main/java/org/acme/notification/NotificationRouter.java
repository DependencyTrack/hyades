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
package org.acme.notification;

import org.acme.exception.PublisherException;
import org.acme.model.Notification;
import org.acme.model.NotificationPublisher;
import org.acme.model.NotificationRule;
import org.acme.model.Project;
import org.acme.model.Team;
import org.acme.notification.publisher.Publisher;
import org.acme.notification.publisher.SendMailPublisher;
import org.acme.notification.vo.AnalysisDecisionChange;
import org.acme.notification.vo.BomConsumedOrProcessed;
import org.acme.notification.vo.NewVulnerabilityIdentified;
import org.acme.notification.vo.NewVulnerableDependency;
import org.acme.notification.vo.PolicyViolationIdentified;
import org.acme.notification.vo.VexConsumedOrProcessed;
import org.acme.notification.vo.ViolationAnalysisDecisionChange;
import org.acme.persistence.NotificationRuleRepository;
import org.acme.persistence.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.transaction.Transactional;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotificationRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRouter.class);

    private final NotificationRuleRepository ruleRepository;
    private final TeamRepository teamRepository;

    public NotificationRouter(final NotificationRuleRepository ruleRepository,
                              final TeamRepository teamRepository) {
        this.ruleRepository = ruleRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public void inform(final Notification notification) {
        for (final NotificationRule rule : resolveRules(notification)) {

            // Not all publishers need configuration (i.e. ConsolePublisher)
            JsonObject config = Json.createObjectBuilder().build();
            if (rule.getPublisherConfig() != null) {
                try (StringReader stringReader = new StringReader(rule.getPublisherConfig());
                     final JsonReader jsonReader = Json.createReader(stringReader)) {
                    config = jsonReader.readObject();
                } catch (Exception e) {
                    LOGGER.error("An error occurred while preparing the configuration for the notification publisher", e);
                }
            }
            try {
                NotificationPublisher notificationPublisher = rule.getPublisher();
                final Class<?> publisherClass = Class.forName(notificationPublisher.getPublisherClass());
                if (Publisher.class.isAssignableFrom(publisherClass)) {
                    // Instead of instantiating publisher classes ad-hoc, look the up in the CDI context.
                    // This way publishers can make use of dependency injection.
                    // TODO: Ensure all publisher implementations are available in CDI
                    final Publisher publisher = (Publisher) CDI.current().select(publisherClass).get();
                    JsonObject notificationPublisherConfig = Json.createObjectBuilder()
                            .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, notificationPublisher.getTemplateMimeType())
                            .add(Publisher.CONFIG_TEMPLATE_KEY, notificationPublisher.getTemplate())
                            .addAll(Json.createObjectBuilder(config))
                            .build();

                    final List<Team> ruleTeams = teamRepository.findByNotificationRule(rule.getId());
                    if (publisherClass != SendMailPublisher.class || ruleTeams.isEmpty()) {
                        publisher.inform(restrictNotificationToRuleProjects(notification, rule), notificationPublisherConfig);
                    } else {
                        ((SendMailPublisher) publisher).inform(restrictNotificationToRuleProjects(notification, rule), notificationPublisherConfig, ruleTeams);
                    }


                } else {
                    LOGGER.error("The defined notification publisher is not assignable from " + Publisher.class.getCanonicalName());
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("An error occurred while instantiating a notification publisher", e);
            } catch (PublisherException publisherException) {
                LOGGER.error("An error occured during the publication of the notification", publisherException);
            }
        }
    }

    public Notification restrictNotificationToRuleProjects(Notification initialNotification, NotificationRule rule) {
        Notification restrictedNotification = initialNotification;
        if (canRestrictNotificationToRuleProjects(initialNotification, rule)) {
            Set<String> ruleProjectsUuids = rule.getProjects().stream().map(Project::getUuid).map(UUID::toString).collect(Collectors.toSet());
            restrictedNotification = new Notification();
            restrictedNotification.setGroup(initialNotification.getGroup());
            restrictedNotification.setLevel(initialNotification.getLevel());
            restrictedNotification.scope(initialNotification.getScope());
            restrictedNotification.setContent(initialNotification.getContent());
            restrictedNotification.setTitle(initialNotification.getTitle());
            restrictedNotification.setTimestamp(initialNotification.getTimestamp());
            if (initialNotification.getSubject() instanceof final NewVulnerabilityIdentified subject) {
                Set<Project> restrictedProjects = subject.getAffectedProjects().stream().filter(project -> ruleProjectsUuids.contains(project.getUuid().toString())).collect(Collectors.toSet());
                NewVulnerabilityIdentified restrictedSubject = new NewVulnerabilityIdentified(subject.getVulnerability(), subject.getComponent(), restrictedProjects, null);
                restrictedNotification.setSubject(restrictedSubject);
            }
        }
        return restrictedNotification;
    }

    private boolean canRestrictNotificationToRuleProjects(Notification initialNotification, NotificationRule rule) {
        return (initialNotification.getSubject() instanceof NewVulnerabilityIdentified || initialNotification.getSubject() instanceof AnalysisDecisionChange) &&
                rule.getProjects() != null
                && rule.getProjects().size() > 0;
    }

    List<NotificationRule> resolveRules(final Notification notification) {
        // The notification rules to process for this specific notification
        final List<NotificationRule> rules = new ArrayList<>();

        if (notification == null || notification.getScope() == null || notification.getGroup() == null || notification.getLevel() == null) {
            return rules;
        }

        final NotificationScope scope;
        try {
            scope = NotificationScope.valueOf(notification.getScope());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid notification scope", e);
            return rules;
        }

        final List<NotificationRule> result = ruleRepository.findByScopeAndForLevel(scope, notification.getLevel());
        if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())
                && notification.getSubject() instanceof final NewVulnerabilityIdentified subject) {
            // If the rule specified one or more projects as targets, reduce the execution
            // of the notification down to those projects that the rule matches and which
            // also match project the component is included in.
            // NOTE: This logic is slightly different from what is implemented in limitToProject()
            for (final NotificationRule rule : result) {
                if (rule.getNotifyOn().contains(NotificationGroup.valueOf(notification.getGroup()))) {
                    if (rule.getProjects() != null && rule.getProjects().size() > 0
                            && subject.getComponent() != null && subject.getComponent().getProject() != null) {
                        for (final Project project : rule.getProjects()) {
                            if (subject.getComponent().getProject().getUuid().equals(project.getUuid()) || (Boolean.TRUE.equals(rule.isNotifyChildren() && checkIfChildrenAreAffected(project, subject.getComponent().getProject().getUuid())))) {
                                rules.add(rule);
                            }
                        }
                    } else {
                        rules.add(rule);
                    }
                }
            }
        } else if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())
                && notification.getSubject() instanceof final NewVulnerableDependency subject) {
            limitToProject(rules, result, notification, subject.getComponent().getProject());
        } else if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())
                && notification.getSubject() instanceof final BomConsumedOrProcessed subject) {
            limitToProject(rules, result, notification, subject.getProject());
        } else if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())
                && notification.getSubject() instanceof final VexConsumedOrProcessed subject) {
            limitToProject(rules, result, notification, subject.getProject());
        } else if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())
                && notification.getSubject() instanceof final PolicyViolationIdentified subject) {
            limitToProject(rules, result, notification, subject.getProject());
        } else if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())
                && notification.getSubject() instanceof final AnalysisDecisionChange subject) {
            limitToProject(rules, result, notification, subject.getProject());
        } else if (NotificationScope.PORTFOLIO.name().equals(notification.getScope())
                && notification.getSubject() instanceof final ViolationAnalysisDecisionChange subject) {
            limitToProject(rules, result, notification, subject.getComponent().getProject());
        } else {
            for (final NotificationRule rule : result) {
                if (rule.getNotifyOn().contains(NotificationGroup.valueOf(notification.getGroup()))) {
                    rules.add(rule);
                }
            }
        }
        return rules;
    }

    /**
     * if the rule specified one or more projects as targets, reduce the execution
     * of the notification down to those projects that the rule matches and which
     * also match projects affected by the vulnerability.
     */
    private void limitToProject(final List<NotificationRule> applicableRules, final List<NotificationRule> rules,
                                final Notification notification, final Project limitToProject) {
        for (final NotificationRule rule : rules) {
            if (rule.getNotifyOn().contains(NotificationGroup.valueOf(notification.getGroup()))) {
                if (rule.getProjects() != null && rule.getProjects().size() > 0) {
                    for (final Project project : rule.getProjects()) {
                        if (project.getUuid().equals(limitToProject.getUuid()) || (Boolean.TRUE.equals(rule.isNotifyChildren()) && checkIfChildrenAreAffected(project, limitToProject.getUuid()))) {
                            applicableRules.add(rule);
                        }
                    }
                } else {
                    applicableRules.add(rule);
                }
            }
        }
    }

    private boolean checkIfChildrenAreAffected(Project parent, UUID uuid) {
        boolean isChild = false;
        if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
            return false;
        }
        for (Project child : parent.getChildren()) {
            if ((child.getUuid().equals(uuid) && child.isActive()) || isChild) {
                return true;
            }
            isChild = checkIfChildrenAreAffected(child, uuid);
        }
        return isChild;
    }
}
