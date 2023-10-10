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
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.HttpClients;
import org.hyades.commonutil.DateUtil;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class NugetMetaAnalyzerTest {

    private static ClientAndServer mockServer;

    private IMetaAnalyzer analyzer;

    @BeforeAll
    static void beforeClass() {
        mockServer = ClientAndServer.startClientAndServer(1080);
    }

    @BeforeEach
    void beforeEach() {
        analyzer = new NugetMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
    }

    @AfterAll
    static void afterClass() {
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

        final var component = new Component();
        component.setPurl(new PackageURL("pkg:nuget/NUnitPrivate@2.0.1"));
        analyzer.setRepositoryUsernameAndPassword(null, "password");
        analyzer.setRepositoryBaseUrl("http://localhost:1080");
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getComponent());
    }


    @Test
    void testAnalyzer() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:nuget/NUnit@3.8.0"));
        Assertions.assertEquals("NugetMetaAnalyzer", analyzer.getName());

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
                "-nunit-index.json");
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v3/flat2/nunit/index.json")
                                .withHeader("Authorization", encodedBasicHeader)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(mockVersionResponse)
                );

        String mockRegistrationResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3" +
                "-registrations2-nunit-400.json");
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/v3/registrations2/nunit/4.0.0.json")
                                .withHeader("Authorization", encodedBasicHeader)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(mockRegistrationResponse)
                );

        analyzer.setRepositoryUsernameAndPassword(null, "password");
        analyzer.setRepositoryBaseUrl("http://localhost:1080");
        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertTrue(analyzer.isApplicable(component));
        Assertions.assertEquals(RepositoryType.NUGET, analyzer.supportedRepositoryType());
        Assertions.assertNotNull(metaModel.getComponent());
        Assertions.assertNotNull(metaModel.getLatestVersion());
        Assertions.assertEquals("4.0.0", metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Assertions.assertEquals(dateFormat.parse("2022-04-13T13:30:25Z"), metaModel.getPublishedTimestamp());
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

        final var component = new Component();
        component.setPurl(new PackageURL("pkg:nuget/NUnitPrivate@5.0.1"));
        analyzer.setRepositoryUsernameAndPassword(null, "password");
        analyzer.setRepositoryBaseUrl("http://localhost:1080");
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getComponent());
        Assertions.assertEquals("5.0.2", metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
    }


    private String readResourceFileToString(String fileName) throws Exception {
        return Files.readString(Paths.get(getClass().getResource(fileName).toURI()));
    }
}
