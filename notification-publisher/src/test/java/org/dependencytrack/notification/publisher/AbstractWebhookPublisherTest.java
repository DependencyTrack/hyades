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
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.notification.publisher;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.json.JsonObjectBuilder;
import org.dependencytrack.notification.util.WireMockTestResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

@QuarkusTestResource(WireMockTestResource.class)
abstract class AbstractWebhookPublisherTest<T extends AbstractWebhookPublisher> extends AbstractPublisherTest<T> {

    @WireMockTestResource.InjectWireMock
    WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() {
        wireMockServer.stubFor(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @Override
    JsonObjectBuilder extraConfig() {
        return super.extraConfig()
                .add(Publisher.CONFIG_DESTINATION, wireMockServer.baseUrl());
    }

}
