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
package org.acme.repositories;

import com.github.packageurl.PackageURL;
import org.acme.model.Component;
import org.acme.model.RepositoryType;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class NugetMetaAnalyzerTest {

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void beforeClass() {
        mockServer = ClientAndServer.startClientAndServer(1080);
    }

    @AfterAll
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    void testAnalyzer() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:nuget/NUnit@3.8.0"));
        NugetMetaAnalyzer analyzer = new NugetMetaAnalyzer();

        analyzer.setRepositoryBaseUrl("https://api.nuget.org");
        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertTrue(analyzer.isApplicable(component));
        Assertions.assertEquals(RepositoryType.NUGET, analyzer.supportedRepositoryType());
        Assertions.assertNotNull(metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
    }

    @Test
    void testAnalyzerWithPrivatePackageRepository() throws Exception {
        String mockIndexResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3-index.json");
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v3/index.json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(mockIndexResponse)
                );

        String encodedBasicHeader = "Basic bnVsbDpwYXNzd29yZA==";
        String mockVersionResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3-flat2" +
                "-nunitprivate-index.json");
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v3/flat2/nunitprivate/index.json")
                                .withHeader("Authorization", encodedBasicHeader)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(mockVersionResponse)
                );

        String mockRegistrationResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3" +
                "-registrations2-nunitprivate-502.json");
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v3/registrations2/nunitprivate/5.0.2.json")
                                .withHeader("Authorization", encodedBasicHeader)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(mockRegistrationResponse)
                );

        Component component = new Component();
        component.setPurl(new PackageURL("pkg:nuget/NUnitPrivate@5.0.1"));
        NugetMetaAnalyzer analyzer = new NugetMetaAnalyzer();
        analyzer.setRepositoryUsernameAndPassword(null, "password");
        analyzer.setRepositoryBaseUrl("http://localhost:1080");
        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertEquals("5.0.2", metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
    }

    private String readResourceFileToString(String fileName) throws Exception {
        return Files.readString(Paths.get(getClass().getResource(fileName).toURI()));
    }
}
