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
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.HttpClients;
import org.dependencytrack.repometaanalyzer.model.MetaModel;
import org.dependencytrack.persistence.model.Component;
import org.dependencytrack.persistence.model.RepositoryType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class NpmMetaAnalyzerTest {

    private static ClientAndServer mockServer;

    private IMetaAnalyzer analyzer;

    @BeforeAll
    static void beforeClass() {
        mockServer = ClientAndServer.startClientAndServer(1080);
    }

    @BeforeEach
    void beforeEach() {
        analyzer = new NpmMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
    }

    @AfterEach
    void afterEach() {
        mockServer.reset();
    }

    @AfterAll
    static void afterClass() {
        mockServer.stop();
    }

    @Test
    void testAnalyzer() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:npm/qunit@2.7.0"));

        Assertions.assertEquals("NpmMetaAnalyzer", analyzer.getName());
        Assertions.assertTrue(analyzer.isApplicable(component));
        Assertions.assertEquals(RepositoryType.NPM, analyzer.supportedRepositoryType());
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getLatestVersion());
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

        Assertions.assertNull(metaModel.getLatestVersion());
        Assertions.assertNull(
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

        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertNull(metaModel.getLatestVersion());
        Assertions.assertNull(
                metaModel.getPublishedTimestamp()
        );
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

        Assertions.assertNull(metaModel.getLatestVersion());
        Assertions.assertNull(
                metaModel.getPublishedTimestamp()
        );
    }

    @Test
    void testAnalyzerReturnIntegrityResult() {
        Component component = new Component();
        component.setPurl("pkg:npm/typo3/package-empty-result@v1.2.0");
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
                                .withHeader("X-Checksum-SHA512", "sha512hash")
                                .withHeader("Last-Modified", "Thu, 07 Jul 2022 14:00:00 GMT")
                );

        var integrityMeta = analyzer.getIntegrityMeta(component);
        assertNotNull(integrityMeta);
        assertThat(integrityMeta.getMetaSourceUrl()).contains("/typo3/package-empty-result/-/typo3/package-empty-result-v1.2.0.tgz");
        assertEquals("md5hash", integrityMeta.getMd5());
        assertEquals("sha1hash", integrityMeta.getSha1());
        assertEquals("sha256hash", integrityMeta.getSha256());
        assertEquals("sha512hash", integrityMeta.getSha512());
        assertNotNull(integrityMeta.getCurrentVersionLastModified());
    }

    @Test
    void testIntegrityResultForPurlWithoutNamespace() {
        Component component = new Component();
        component.setPurl("pkg:npm/amazon-s3-uri@0.0.1");
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
                );

        var integrityMeta = analyzer.getIntegrityMeta(component);
        assertNotNull(integrityMeta);
        assertThat(integrityMeta.getMetaSourceUrl()).contains("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz");
    }

    @Test
    void testIntegrityAnalyzerException() {
        Component component = new Component();
        component.setPurl("pkg:npm/amazon-s3-uri@0.0.1");
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", mockServer.getPort()));
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("HEAD")
                                .withPath("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz")
                )
                .respond(
                        response()
                                .withStatusCode(400)
                );
        var integrityMeta = analyzer.getIntegrityMeta(component);
        assertNotNull(integrityMeta);
        assertThat(integrityMeta.getMetaSourceUrl()).contains("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz");
        assertNull(integrityMeta.getSha1());
        assertNull(integrityMeta.getMd5());
        assertNull(integrityMeta.getSha256());
        assertNull(integrityMeta.getSha512());
        assertNull(integrityMeta.getCurrentVersionLastModified());
    }
}
