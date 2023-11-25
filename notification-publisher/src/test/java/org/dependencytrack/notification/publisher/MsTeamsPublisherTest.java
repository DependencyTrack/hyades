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
package org.dependencytrack.notification.publisher;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import org.apache.http.HttpHeaders;
import org.dependencytrack.proto.notification.v1.Group;
import org.dependencytrack.proto.notification.v1.Level;
import org.dependencytrack.proto.notification.v1.Notification;
import org.dependencytrack.proto.notification.v1.Scope;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.dependencytrack.notification.publisher.PublisherTestUtil.createPublisherContext;
import static org.dependencytrack.notification.publisher.PublisherTestUtil.getConfig;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@QuarkusTest
public class MsTeamsPublisherTest {

    @Inject
    MsTeamsPublisher publisher;

    @Inject
    EntityManager entityManager;

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void beforeClass() {
        mockServer = startClientAndServer(1060);
    }

    @AfterAll
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    @TestTransaction
    public void testPublish() throws Exception {
        new MockServerClient("localhost", 1060)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/mychannel")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                );
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('msteams', 'general', 'STRING', 'base.url', 'http://localhost:1060/mychannel');
                """).executeUpdate();

        JsonObject config = getConfig("MS_TEAMS","http://localhost:1060/mychannel");
        final var notification = Notification.newBuilder()
                .setScope(Scope.SCOPE_PORTFOLIO)
                .setLevel(Level.LEVEL_INFORMATIONAL)
                .setGroup(Group.GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();
        publisher.inform(createPublisherContext(notification), notification, config);
    }
}
