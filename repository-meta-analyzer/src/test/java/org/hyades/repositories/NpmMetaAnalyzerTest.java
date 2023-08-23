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
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class NpmMetaAnalyzerTest {

    private static ClientAndServer mockServer;

    private NpmMetaAnalyzer analyzer;

    @BeforeAll
    static void beforeClass() {
        mockServer = ClientAndServer.startClientAndServer(1080);
    }

    @BeforeEach
    void beforeEach() {
        analyzer = new NpmMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
    }

    @AfterAll
    static void afterClass() {
        mockServer.stop();
    }

    @Test
    void testAnalyzer() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:npm/qunit@2.7.0"));

        assertEquals("NpmMetaAnalyzer", analyzer.getName());
        assertTrue(analyzer.isApplicable(component));
        assertEquals(RepositoryType.NPM, analyzer.supportedRepositoryType());
        MetaModel metaModel = analyzer.analyze(component);
        assertNotNull(metaModel.getLatestVersion());
        //Assert.assertNotNull(metaModel.getPublishedTimestamp()); // todo: not yet supported
    }

    @Test
    void testAnalyzerDoesNotFindResult() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:npm/package-does-not-exist@v1.2.0"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", mockServer.getPort()));
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody("Not found")
                );

        MetaModel metaModel = analyzer.analyze(component);

        assertNull(metaModel.getLatestVersion());
        assertNull(
                metaModel.getPublishedTimestamp()
        );
    }

    @Test
    void testAnalyzerReturnEmptyResult() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:npm/typo3/package-empty-result@v1.2.0"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", mockServer.getPort()));
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/p/typo3/package-empty-result.json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                );

        var metaModel = analyzer.analyze(component);

        assertNull(metaModel.getLatestVersion());
        assertNull(
                metaModel.getPublishedTimestamp()
        );
    }

    @Test
    void testAnalyzerReturnIntegrityResult() {
        Component component = new Component();
        component.setUuid(UUID.randomUUID());
        component.setPurl("pkg:npm/typo3/package-empty-result@v1.2.0");
        component.setMd5("md5hash");
        component.setSha1("sha1hash");
        component.setSha256("sha256hash");
        component.setInternal(true);
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", mockServer.getPort()));
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("HEAD")
                                .withPath("/typo3/package-empty-result/-/typo3/package-empty-result-v1.2.0.tgz")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("X-Checksum-MD5", "md5hash")
                                .withHeader("X-Checksum-SHA1", "sha1hash")
                                .withHeader("X-Checksum-SHA256", "sha256hash")
                );

        var integrityModel = analyzer.getIntegrityModel(component);

        assertNotNull(integrityModel);
        assertEquals("pkg:npm/typo3/package-empty-result@v1.2.0", integrityModel.getComponent().getPurl().toString());
        assertEquals("md5hash", integrityModel.getComponent().getMd5());
        assertEquals("sha1hash", integrityModel.getComponent().getSha1());
        assertEquals("sha256hash", integrityModel.getComponent().getSha256());
    }

    @Test
    void testIntegrityResultForPurlWithoutNamespace() {
        Component component = new Component();
        component.setUuid(UUID.randomUUID());
        component.setPurl("pkg:npm/amazon-s3-uri@0.0.1");
        component.setMd5("md5hash");
        component.setSha1("sha1hash");
        component.setSha256("sha256hash");
        component.setInternal(true);
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", mockServer.getPort()));
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("HEAD")
                                .withPath("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("X-Checksum-MD5", "md5hash")
                                .withHeader("X-Checksum-SHA1", "sha1hash")
                                .withHeader("X-Checksum-SHA256", "sha256hash")
                );

        var integrityModel = analyzer.getIntegrityModel(component);

        assertNotNull(integrityModel);
        assertEquals("pkg:npm/amazon-s3-uri@0.0.1", integrityModel.getComponent().getPurl().toString());
        assertEquals("md5hash", integrityModel.getComponent().getMd5());
        assertEquals("sha1hash", integrityModel.getComponent().getSha1());
        assertEquals("sha256hash", integrityModel.getComponent().getSha256());
    }

    @Test
    void testAnalyzerReturnEmptyResultWithBraces() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:npm/typo3/package-empty-result@v1.2.0"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", mockServer.getPort()));
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/p/typo3/package-empty-result.json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody("{}")
                );

        MetaModel metaModel = analyzer.analyze(component);

        assertNull(metaModel.getLatestVersion());
        assertNull(
                metaModel.getPublishedTimestamp()
        );
    }
}
