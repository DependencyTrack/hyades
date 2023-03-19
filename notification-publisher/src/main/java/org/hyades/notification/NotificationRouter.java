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
package org.hyades.notification;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import org.hyades.commonnotification.NotificationScope;
import org.hyades.exception.PublisherException;
import org.hyades.model.NotificationPublisher;
import org.hyades.model.NotificationRule;
import org.hyades.model.Project;
import org.hyades.model.Team;
import org.hyades.notification.publisher.Publisher;
import org.hyades.notification.publisher.SendMailPublisher;
import org.hyades.persistence.NotificationRuleRepository;
import org.hyades.persistence.TeamRepository;
import org.hyades.proto.notification.v1.BomConsumedOrProcessedSubject;
import org.hyades.proto.notification.v1.NewVulnerabilitySubject;
import org.hyades.proto.notification.v1.NewVulnerableDependencySubject;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.PolicyViolationAnalysisDecisionChangeSubject;
import org.hyades.proto.notification.v1.PolicyViolationSubject;
import org.hyades.proto.notification.v1.VexConsumedOrProcessedSubject;
import org.hyades.proto.notification.v1.VulnerabilityAnalysisDecisionChangeSubject;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.UnsatisfiedResolutionException;
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

import static org.hyades.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.hyades.proto.notification.v1.Scope.SCOPE_SYSTEM;
import static org.hyades.util.ModelConverter.convert;

@ApplicationScoped
public class NotificationRouter {

    private static final Logger LOGGER = Logger.getLogger(NotificationRouter.class);

    private final NotificationRuleRepository ruleRepository;
    private final TeamRepository teamRepository;

    public NotificationRouter(final NotificationRuleRepository ruleRepository,
                              final TeamRepository teamRepository) {
        this.ruleRepository = ruleRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public void inform(final Notification notification) throws Exception {
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
                // final Class<?> publisherClass = Class.forName(notificationPublisher.getPublisherClass());
                // FIXME: The fully qualified class name is not known by us, because it refers to the org.dependencytrack package
                final Class<?> publisherClass = Class.forName(notificationPublisher.getPublisherClass().replaceAll("^org\\.dependencytrack\\.", "org.hyades."));
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
            } catch (ClassNotFoundException | UnsatisfiedResolutionException e) {
                LOGGER.error("An error occurred while instantiating a notification publisher", e);
            } catch (PublisherException publisherException) {
                LOGGER.error("An error occured during the publication of the notification", publisherException);
            }
        }
    }

    public Notification restrictNotificationToRuleProjects(Notification initialNotification, NotificationRule rule) throws InvalidProtocolBufferException {
        Notification.Builder restrictedNotification = Notification.newBuilder(initialNotification);
        if (canRestrictNotificationToRuleProjects(initialNotification, rule)) {
            Set<String> ruleProjectsUuids = rule.getProjects().stream().map(Project::getUuid).map(UUID::toString).collect(Collectors.toSet());
            if (initialNotification.getSubject().is(NewVulnerabilitySubject.class)) {
                final var initialSubject = initialNotification.getSubject().unpack(NewVulnerabilitySubject.class);
                Set<org.hyades.proto.notification.v1.Project> restrictedProjects = initialSubject.getAffectedProjectsList().stream().filter(project -> ruleProjectsUuids.contains(project.getUuid())).collect(Collectors.toSet());
                restrictedNotification.setSubject(Any.pack(NewVulnerabilitySubject.newBuilder(initialSubject)
                        .clearAffectedProjects()
                        .addAllAffectedProjects(restrictedProjects)
                        .build()));
            }
        }
        return restrictedNotification.build();
    }

    private boolean canRestrictNotificationToRuleProjects(Notification initialNotification, NotificationRule rule) {
        return (initialNotification.getSubject().is(NewVulnerabilitySubject.class)
                || initialNotification.getSubject().is(VulnerabilityAnalysisDecisionChangeSubject.class))
                && rule.getProjects() != null
                && rule.getProjects().size() > 0;
    }

    List<NotificationRule> resolveRules(final Notification notification) throws InvalidProtocolBufferException {
        // The notification rules to process for this specific notification
        final List<NotificationRule> rules = new ArrayList<>();

        if (notification == null) {
            return rules;
        }


        final NotificationScope scope;
        if (notification.getScope() == SCOPE_PORTFOLIO) {
            scope = NotificationScope.PORTFOLIO;
        } else if (notification.getScope() == SCOPE_SYSTEM) {
            scope = NotificationScope.SYSTEM;
        } else {
            LOGGER.error("Invalid notification scope: " + notification.getScope());
            return rules;
        }

        final List<NotificationRule> result = ruleRepository.findByScopeAndForLevel(scope, convert(notification.getLevel()));
        if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(NewVulnerabilitySubject.class)) {
            final var subject = notification.getSubject().unpack(NewVulnerabilitySubject.class);
            // If the rule specified one or more projects as targets, reduce the execution
            // of the notification down to those projects that the rule matches and which
            // also match project the component is included in.
            // NOTE: This logic is slightly different from what is implemented in limitToProject()
            for (final NotificationRule rule : result) {
                if (rule.getNotifyOn().contains(convert(notification.getGroup()))) {
                    if (rule.getProjects() != null && rule.getProjects().size() > 0
                            && subject.hasComponent() && subject.hasProject()) {
                        for (final Project project : rule.getProjects()) {
                            if (subject.getProject().getUuid().equals(project.getUuid().toString()) || (Boolean.TRUE.equals(rule.isNotifyChildren() && checkIfChildrenAreAffected(project, subject.getProject().getUuid())))) {
                                rules.add(rule);
                            }
                        }
                    } else {
                        rules.add(rule);
                    }
                }
            }
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(NewVulnerableDependencySubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(NewVulnerableDependencySubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(BomConsumedOrProcessedSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(BomConsumedOrProcessedSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(VexConsumedOrProcessedSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(VexConsumedOrProcessedSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(PolicyViolationSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(PolicyViolationSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(VulnerabilityAnalysisDecisionChangeSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(VulnerabilityAnalysisDecisionChangeSubject.class).getProject());
        } else if (notification.getScope() == SCOPE_PORTFOLIO
                && notification.getSubject().is(PolicyViolationAnalysisDecisionChangeSubject.class)) {
            limitToProject(rules, result, notification, notification.getSubject().unpack(PolicyViolationAnalysisDecisionChangeSubject.class).getProject());
        } else {
            for (final NotificationRule rule : result) {
                if (rule.getNotifyOn().contains(convert(notification.getGroup()))) {
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
                                final Notification notification, final org.hyades.proto.notification.v1.Project limitToProject) {
        for (final NotificationRule rule : rules) {
            if (rule.getNotifyOn().contains(convert(notification.getGroup()))) {
                if (rule.getProjects() != null && rule.getProjects().size() > 0) {
                    for (final Project project : rule.getProjects()) {
                        if (project.getUuid().toString().equals(limitToProject.getUuid()) || (Boolean.TRUE.equals(rule.isNotifyChildren()) && checkIfChildrenAreAffected(project, limitToProject.getUuid()))) {
                            applicableRules.add(rule);
                        }
                    }
                } else {
                    applicableRules.add(rule);
                }
            }
        }
    }

    private boolean checkIfChildrenAreAffected(Project parent, String uuid) {
        boolean isChild = false;
        if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
            return false;
        }
        for (Project child : parent.getChildren()) {
            if ((child.getUuid().toString().equals(uuid) && child.isActive()) || isChild) {
                return true;
            }
            isChild = checkIfChildrenAreAffected(child, uuid);
        }
        return isChild;
    }
}
