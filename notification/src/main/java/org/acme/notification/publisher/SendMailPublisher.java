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
package org.acme.notification.publisher;

import alpine.common.logging.Logger;
import alpine.common.util.BooleanUtil;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.acme.model.ConfigProperty;
import org.acme.model.ConfigPropertyConstants;
import org.acme.model.Notification;
import org.acme.model.Team;
import org.acme.persistence.ConfigPropertyRepository;
import org.acme.persistence.ManagedUserRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;

@ApplicationScoped
public class SendMailPublisher implements Publisher {

    private static final Logger LOGGER = Logger.getLogger(SendMailPublisher.class);
    private static final PebbleEngine ENGINE = new PebbleEngine.Builder().newLineTrimming(false).build();

    private final ManagedUserRepository managedUserRepository;

    private final ConfigPropertyRepository configPropertyRepository;

    @Inject
    Mailer mailer;
    public SendMailPublisher(final ManagedUserRepository managedUserRepository, final ConfigPropertyRepository configPropertyRepository) {
        this.managedUserRepository = managedUserRepository;
        this.configPropertyRepository = configPropertyRepository;
    }

    public void inform(final Notification notification, final JsonObject config) {
        if (config == null) {
            LOGGER.warn("No configuration found. Skipping notification.");
            return;
        }
        final String[] destinations = parseDestination(config);
        sendNotification(notification, config, destinations);
    }

    public void inform(final Notification notification, final JsonObject config, List<Team> teams) {
        if (config == null) {
            LOGGER.warn("No configuration found. Skipping notification.");
            return;
        }
        final String[] destinations = parseDestination(config, teams);
        sendNotification(notification, config, destinations);
    }

    private void sendNotification(Notification notification, JsonObject config, String[] destinations) {
        PebbleTemplate template = getTemplate(config);
        String mimeType = getTemplateMimeType(config);
        final String content = prepareTemplate(notification, template, configPropertyRepository);
        if (destinations == null || content == null) {
            LOGGER.warn("A destination or template was not found. Skipping notification");
            return;
        }
        try {
            ConfigProperty smtpEnabledConfig = configPropertyRepository.findByGroupAndName(ConfigPropertyConstants.EMAIL_SMTP_ENABLED.getGroupName(), ConfigPropertyConstants.EMAIL_SMTP_ENABLED.getPropertyName());
            boolean smtpEnabled = BooleanUtil.valueOf(smtpEnabledConfig.getPropertyValue());
            if (!smtpEnabled) {
                LOGGER.warn("SMTP is not enabled");
                return; // smtp is not enabled
            }
            for (String destination: destinations){
                mailer.send(Mail.withText(destination, "\"[Dependency-Track] \" + notification.getTitle()", content));
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred sending output email notification", e);
        }
    }

    @Override
    public PebbleEngine getTemplateEngine() {
        return ENGINE;
    }

    static String[] parseDestination(final JsonObject config) {
        String destinationString = config.getString("destination");
        if ((destinationString == null) || destinationString.isEmpty()) {
            return null;
        }
        return destinationString.split(",");
    }

    String[] parseDestination(final JsonObject config, final List<Team> teams) {
        String[] destination = teams.stream().flatMap(
                        team -> Stream.of(
                                        Arrays.stream(config.getString("destination").split(",")).filter(Predicate.not(String::isEmpty)),
                                        managedUserRepository.findEmailsByTeam(team.getId()).stream()
                                        // FIXME: The email field of LdapUser and OidcUser is transient and not persisted in the DB
                                        // Maybe this was always a no-op and can safely be removed.
                                        //Optional.ofNullable(team.getLdapUsers()).orElseGet(Collections::emptyList).stream().map(LdapUser::getEmail).filter(Objects::nonNull),
                                        //Optional.ofNullable(team.getOidcUsers()).orElseGet(Collections::emptyList).stream().map(OidcUser::getEmail).filter(Objects::nonNull)
                                )
                                .reduce(Stream::concat)
                                .orElseGet(Stream::empty)
                )
                .distinct()
                .toArray(String[]::new);
        return destination.length == 0 ? null : destination;
    }

}
