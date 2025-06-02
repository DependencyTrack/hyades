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
package org.dependencytrack.common;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@QuarkusTest
@TestProfile(org.dependencytrack.common.HttpClientConfigWithNoProxyTest.TestProfile.class)
@ConnectWireMock
class HttpClientConfigWithNoProxyTest {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "client.http.config.proxy-timeout-connection", "20",
                    "client.http.config.proxy-timeout-pool", "40",
                    "client.http.config.proxy-timeout-socket", "20",
                    "client.http.config.proxy-username", "test",
                    "client.http.config.proxy-password", "test",
                    "client.http.config.proxy-address", "http://localhost",
                    "client.http.config.proxy-port", "1080",
                    "client.http.config.no-proxy", "http://localhost:8080,*"
            );
        }

    }

    @Inject
    HttpClientConfiguration configuration;
    @Inject
    MeterRegistry meterRegistry;

    WireMock wireMock;

    @AfterEach
    void afterEach() {
        wireMock.resetToDefaultMappings();
    }

    @Test
    void clientCreatedWithProxyInfoTest() throws IOException {
        try (CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry)) {
            wireMock.register(get(urlPathEqualTo("/hello"))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .withResponseBody(Body.ofBinaryOrText("hello test".getBytes(),
                                    new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
            HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
            try (CloseableHttpResponse response = client.execute(request)) {
                Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
                String stringResponse = EntityUtils.toString(response.getEntity());
                Assertions.assertEquals("hello test", stringResponse);
            } catch (IOException ex) {
                System.out.println("exception occurred: " + ex.getMessage());
            }
        }
    }

}
