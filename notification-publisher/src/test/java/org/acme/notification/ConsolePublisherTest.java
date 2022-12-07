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

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.commonnotification.NotificationGroup;
import org.acme.commonnotification.NotificationScope;
import org.acme.model.Notification;
import org.acme.model.NotificationLevel;
import org.acme.notification.publisher.ConsolePublisher;
import org.acme.notification.publisher.DefaultNotificationPublishers;
import org.acme.notification.publisher.Publisher;
import org.acme.persistence.ConfigPropertyRepository;
import org.acme.util.NotificationUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;

@QuarkusTest
public class ConsolePublisherTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Inject
    ConfigPropertyRepository configPropertyRepository;

    @Inject
    EntityManager entityManager;


    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    @TestTransaction
    public void testOutputStream() throws IOException {

        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("ID", "DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    (1, 'console', 'general', 'STRING', 'base.url', '');
                """).executeUpdate();

        Notification notification = new Notification();
        notification.setScope(NotificationScope.PORTFOLIO.name());
        notification.setGroup(NotificationGroup.NEW_VULNERABILITY.name());
        notification.setLevel(NotificationLevel.INFORMATIONAL);
        notification.setTitle("Test Notification");
        notification.setContent("This is only a test");
        ConsolePublisher publisher = new ConsolePublisher(configPropertyRepository);
        publisher.inform(notification, getConfig(DefaultNotificationPublishers.CONSOLE, ""));
        Assertions.assertTrue(outContent.toString().contains(expectedResult(notification)));
    }

    @Test
    @TestTransaction
    public void testErrorStream() throws IOException {
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("ID", "DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    (1, 'console', 'general', 'STRING', 'base.url', '');
                """).executeUpdate();

        Notification notification = new Notification();
        notification.setScope(NotificationScope.SYSTEM.name());
        notification.setGroup(NotificationGroup.FILE_SYSTEM.name());
        notification.setLevel(NotificationLevel.ERROR);
        notification.setTitle("Test Notification");
        notification.setContent("This is only a test");
        ConsolePublisher publisher = new ConsolePublisher(configPropertyRepository);
        publisher.inform(notification, getConfig(DefaultNotificationPublishers.CONSOLE, ""));
        Assertions.assertTrue(errContent.toString().contains(expectedResult(notification)));
    }

    private String expectedResult(Notification notification) {
        return "--------------------------------------------------------------------------------" + System.lineSeparator() +
                "Notification" + System.lineSeparator() +
                "  -- timestamp: " + notification.getTimestamp() + System.lineSeparator() +
                "  -- level:     " + notification.getLevel() + System.lineSeparator() +
                "  -- scope:     " + notification.getScope() + System.lineSeparator() +
                "  -- group:     " + notification.getGroup() + System.lineSeparator() +
                "  -- title:     " + notification.getTitle() + System.lineSeparator() +
                "  -- content:   " + notification.getContent() + System.lineSeparator() + System.lineSeparator();
    }
    JsonObject getConfig(DefaultNotificationPublishers publisher, String destination) throws IOException {
        File templateFile = new File(URLDecoder.decode(NotificationUtil.class.getResource(publisher.getPublisherTemplateFile()).getFile(), UTF_8.name()));
        String templateContent = FileUtils.readFileToString(templateFile, UTF_8);
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, publisher.getTemplateMimeType())
                .add(Publisher.CONFIG_TEMPLATE_KEY, templateContent)
                .add(Publisher.CONFIG_DESTINATION, destination)
                .addAll(getExtraConfig())
                .build();
    }

    JsonObjectBuilder getExtraConfig() {
        return Json.createObjectBuilder();
    }
}
