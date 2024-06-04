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
package org.dependencytrack.repometaanalyzer.repositories;

import com.github.packageurl.PackageURL;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClients;
import org.dependencytrack.persistence.model.Component;
import org.dependencytrack.persistence.model.RepositoryType;
import org.dependencytrack.repometaanalyzer.model.MetaModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.dependencytrack.repometaanalyzer.repositories.NugetMetaAnalyzer.SUPPORTED_DATE_FORMATS;

class NugetMetaAnalyzerTest {
    private IMetaAnalyzer analyzer;

    static WireMockServer wireMockServer;

    @AfterEach
    void afterEach() {
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

    @BeforeEach
    void beforeEach() {
        analyzer = new NugetMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();
    }

    @Test
    void testPerformVersionCheck() throws Exception {
        String mockIndexResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v4-index1.json");
        wireMockServer.stubFor(get(urlPathEqualTo("/v4/index1.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText(mockIndexResponse.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));


        String encodedBasicHeader = "Basic OnBhc3N3b3Jk";
        wireMockServer.stubFor(get(urlPathEqualTo("/v4/flat2/nunitprivate/index1.json")).withHeader("Authorization", equalTo(encodedBasicHeader))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("".getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_NOT_FOUND)));


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

        wireMockServer.stubFor(get(urlPathEqualTo("/v3/index.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText(mockIndexResponse.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));


        String encodedBasicHeader = "Basic OnBhc3N3b3Jk";

        String mockVersionResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3-flat2" +
                "-nunit-index.json");

        wireMockServer.stubFor(get(urlPathEqualTo("/v3/flat2/nunit/index.json")).withHeader("Authorization", equalTo(encodedBasicHeader))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText(mockVersionResponse.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));

        String mockRegistrationResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3" +
                "-registrations2-nunit-400.json");

        wireMockServer.stubFor(get(urlPathEqualTo("/v3/registrations2/nunit/4.0.0.json")).withHeader("Authorization", equalTo(encodedBasicHeader))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText(mockRegistrationResponse.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));

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
        wireMockServer.stubFor(get(urlPathEqualTo("/v3/index.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText(mockIndexResponse.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));

        String encodedBasicHeader = "Basic OnBhc3N3b3Jk";

        String mockVersionResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3-flat2" +
                "-nunitprivate-index.json");
        wireMockServer.stubFor(get(urlPathEqualTo("/v3/flat2/nunitprivate/index.json")).withHeader("Authorization", equalTo(encodedBasicHeader))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText(mockVersionResponse.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));


        String mockRegistrationResponse = readResourceFileToString("/unit/repositories/https---localhost-1080-v3" +
                "-registrations2-nunitprivate-502.json");

        wireMockServer.stubFor(get(urlPathEqualTo("/v3/registrations2/nunitprivate/5.0.2.json")).withHeader("Authorization", equalTo(encodedBasicHeader))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText(mockRegistrationResponse.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));

        final var component = new Component();
        component.setPurl(new PackageURL("pkg:nuget/NUnitPrivate@5.0.1"));
        analyzer.setRepositoryUsernameAndPassword(null, "password");
        analyzer.setRepositoryBaseUrl("http://localhost:1080");
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getComponent());
        Assertions.assertEquals("5.0.2", metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
    }

    @Test
    void testPublishedDateTimeFormat() throws ParseException {
        Date dateParsed = null;
        for (DateFormat dateFormat : SUPPORTED_DATE_FORMATS) {
            try {
                dateParsed = dateFormat.parse("1900-01-01T00:00:00+00:00");
            } catch (ParseException e) {}
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Assertions.assertEquals(dateFormat.parse("1900-01-01T00:00:00+00:00"), dateParsed);
    }

    private String readResourceFileToString(String fileName) throws Exception {
        return Files.readString(Paths.get(getClass().getResource(fileName).toURI()));
    }
}
