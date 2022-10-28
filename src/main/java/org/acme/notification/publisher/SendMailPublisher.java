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
import alpine.model.*;
import alpine.notification.Notification;
import alpine.security.crypto.DataEncryption;
import alpine.server.mail.SendMail;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.acme.common.ApplicationProperty;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class SendMailPublisher implements Publisher {

    private static final Logger LOGGER = Logger.getLogger(SendMailPublisher.class);
    private static final PebbleEngine ENGINE = new PebbleEngine.Builder().newLineTrimming(false).build();
    @Inject
    static ApplicationProperty applicationProperty;

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
        final String content = prepareTemplate(notification, template);
        if (destinations == null || content == null) {
            LOGGER.warn("A destination or template was not found. Skipping notification");
            return;
        }
        try {
            final boolean smtpEnabled = applicationProperty.smtpEnabled();
            final String smtpFrom = applicationProperty.smtpFromAddress();
            final String smtpHostname = applicationProperty.smtpServerHostname();
            final int smtpPort = applicationProperty.smtpServerPort();
            final String smtpUser = applicationProperty.smtpUsername();
            final String smtpPassword = applicationProperty.smtpPassword();
            final boolean smtpSslTls = applicationProperty.smtpSsltls();
            final boolean smtpTrustCert = applicationProperty.smptTrustcert();

            if (!smtpEnabled) {
                LOGGER.warn("SMTP is not enabled");
                return; // smtp is not enabled
            }
            final boolean smtpAuth = (smtpUser != null && smtpPassword != null);
            final String password = (smtpPassword != null) ? DataEncryption.decryptAsString(smtpPassword) : null;
            final SendMail sendMail = new SendMail()
                    .from(smtpFrom)
                    .to(destinations)
                    .subject("[Dependency-Track] " + notification.getTitle())
                    .body(content)
                    .bodyMimeType(mimeType)
                    .host(smtpHostname)
                    .port(Integer.valueOf(smtpPort))
                    .username(smtpUser)
                    .password(password)
                    .smtpauth(smtpAuth)
                    .useStartTLS(smtpSslTls)
                    .trustCert(smtpTrustCert);
            sendMail.send();
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

    static String[] parseDestination(final JsonObject config, final List<Team> teams) {
        String[] destination = teams.stream().flatMap(
                team -> Stream.of(
                                Arrays.stream(config.getString("destination").split(",")).filter(Predicate.not(String::isEmpty)),
                                Optional.ofNullable(team.getManagedUsers()).orElseGet(Collections::emptyList).stream().map(ManagedUser::getEmail).filter(Objects::nonNull),
                                Optional.ofNullable(team.getLdapUsers()).orElseGet(Collections::emptyList).stream().map(LdapUser::getEmail).filter(Objects::nonNull),
                                Optional.ofNullable(team.getOidcUsers()).orElseGet(Collections::emptyList).stream().map(OidcUser::getEmail).filter(Objects::nonNull)
                        )
                        .reduce(Stream::concat)
                        .orElseGet(Stream::empty)
                )
                .distinct()
                .toArray(String[]::new);
        return destination.length == 0 ? null : destination;
    }

}
