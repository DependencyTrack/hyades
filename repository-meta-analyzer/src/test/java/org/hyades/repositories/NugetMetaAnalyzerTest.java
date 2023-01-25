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
package org.hyades.repositories;

import com.github.packageurl.PackageURL;
import io.quarkus.test.junit.QuarkusTest;
import org.hyades.model.Component;
import org.hyades.model.MetaModel;
import org.hyades.model.RepositoryType;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@QuarkusTest
class NugetMetaAnalyzerTest {

    @Inject
    NugetMetaAnalyzer analyzer;

    Component component = new Component();

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
    void testPerformVersionCheck() throws Exception {
        String mockIndexResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v4-index1.json");
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v4/index1.json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(mockIndexResponse)
                );
        String encodedBasicHeader = "Basic OnBhc3N3b3Jk";

        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v4/flat2/nunitprivate/index1.json")
                                .withHeader("Authorization", encodedBasicHeader)
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );


        component.setPurl(new PackageURL("pkg:nuget/NUnitPrivate@2.0.1"));
        analyzer.setRepositoryUsernameAndPassword(null, "password");
        analyzer.setRepositoryBaseUrl("http://localhost:1080");
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNull(metaModel.getComponent());
    }


    @Test
    void testAnalyzer() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:nuget/NUnit@3.8.0"));
        Assertions.assertEquals("NugetMetaAnalyzer", analyzer.getName());
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
        String encodedBasicHeader = "Basic OnBhc3N3b3Jk";

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

        component.setPurl(new PackageURL("pkg:nuget/NUnitPrivate@5.0.1"));
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
