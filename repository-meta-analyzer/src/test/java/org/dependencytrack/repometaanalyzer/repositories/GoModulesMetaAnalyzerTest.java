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
package org.dependencytrack.repometaanalyzer.repositories;

import com.github.packageurl.PackageURL;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import jakarta.ws.rs.core.MediaType;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

class GoModulesMetaAnalyzerTest {

    private IMetaAnalyzer analyzer;

    static WireMockServer wireMockServer;

    @AfterEach
    void afterEach() {
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

    @BeforeEach
    void beforeEach() {
        analyzer = new GoModulesMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();
    }

    @Test
    void testAnalyzer() throws Exception {
        final var component = new Component();
        component.setVersion("v0.1.0");
        component.setPurl(new PackageURL("pkg:golang/github.com/CycloneDX/cyclonedx-go@v0.3.0"));

        Assertions.assertEquals("GoModulesMetaAnalyzer", analyzer.getName());
        Assertions.assertTrue(analyzer.isApplicable(component));
        Assertions.assertEquals(RepositoryType.GO_MODULES, analyzer.supportedRepositoryType());

        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getLatestVersion());
        Assertions.assertTrue(metaModel.getLatestVersion().startsWith("v"));
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());

        component.setVersion("0.1.0");
        metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getLatestVersion());
        Assertions.assertFalse(metaModel.getLatestVersion().startsWith("v"));
    }

    @Test
    void testCaseEncode() {
        final var analyzer = new GoModulesMetaAnalyzer();

        Assertions.assertEquals("!cyclone!d!x", analyzer.caseEncode("CycloneDX"));
        Assertions.assertEquals("cyclonedx", analyzer.caseEncode("cyclonedx"));
    }

    @Test
    void testAnalyzerDoesNotFindResult() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:golang/package-does-not-exist@v1.2.0"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        wireMockServer.stubFor(get(urlPathEqualTo("/p/typo3/package-empty-result.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withResponseBody(Body.ofBinaryOrText("Not found".getBytes(),
                                new ContentTypeHeader(MediaType.APPLICATION_JSON))).withStatus(HttpStatus.SC_NOT_FOUND)));

        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertNull(metaModel.getLatestVersion());
        Assertions.assertNull(
                metaModel.getPublishedTimestamp()
        );
    }

    @Test
    void testAnalyzerReturnEmptyResult() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:golang/typo3/package-empty-result@v1.2.0"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));

        wireMockServer.stubFor(get(urlPathEqualTo("/p/typo3/package-empty-result.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withResponseBody(Body.ofBinaryOrText("".getBytes(),
                                new ContentTypeHeader(MediaType.APPLICATION_JSON))).withStatus(HttpStatus.SC_OK)));

        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertNull(metaModel.getLatestVersion());
        Assertions.assertNull(
                metaModel.getPublishedTimestamp()
        );
    }

    @Test
    void testAnalyzerReturnEmptyResultWithBraces() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:golang/typo3/package-empty-result@v1.2.0"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));

        wireMockServer.stubFor(get(urlPathEqualTo("/p/typo3/package-empty-result.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withResponseBody(Body.ofBinaryOrText("{}".getBytes(),
                                new ContentTypeHeader(MediaType.APPLICATION_JSON))).withStatus(HttpStatus.SC_OK)));

        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertNull(metaModel.getLatestVersion());
        Assertions.assertNull(
                metaModel.getPublishedTimestamp()
        );
    }
}
