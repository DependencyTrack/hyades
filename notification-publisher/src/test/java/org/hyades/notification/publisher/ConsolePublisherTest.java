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
package org.hyades.notification.publisher;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.hyades.proto.notification.v1.Group;
import org.hyades.proto.notification.v1.Level;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.Scope;
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
import java.io.IOException;
import java.io.PrintStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyades.proto.notification.v1.Group.GROUP_FILE_SYSTEM;
import static org.hyades.proto.notification.v1.Level.LEVEL_ERROR;
import static org.hyades.proto.notification.v1.Scope.SCOPE_SYSTEM;

@QuarkusTest
public class ConsolePublisherTest {

    @Inject
    ConsolePublisher publisher;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

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
    public void testOutputStream() throws Exception {
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('console', 'general', 'STRING', 'base.url', '');
                """).executeUpdate();

        final var notification = Notification.newBuilder()
                .setScope(Scope.SCOPE_PORTFOLIO)
                .setLevel(Level.LEVEL_INFORMATIONAL)
                .setGroup(Group.GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();
        publisher.inform(notification, getConfig(DefaultNotificationPublishers.CONSOLE, ""));
        Assertions.assertTrue(outContent.toString().contains(expectedResult(notification)));
    }

    @Test
    @TestTransaction
    public void testErrorStream() throws Exception {
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('console', 'general', 'STRING', 'base.url', '');
                """).executeUpdate();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_SYSTEM)
                .setGroup(GROUP_FILE_SYSTEM)
                .setLevel(LEVEL_ERROR)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();
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
        String templateContent = IOUtils.resourceToString(publisher.getPublisherTemplateFile(), UTF_8);
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
