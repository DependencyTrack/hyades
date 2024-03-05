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
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class NpmMetaAnalyzerTest {
    static WireMockServer wireMockServer;

    @AfterEach
    void afterEach() {
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

    @BeforeEach
    void beforeEach() {
        analyzer = new NpmMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();
    }

    private IMetaAnalyzer analyzer;

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
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));

        wireMockServer.stubFor(get(urlPathEqualTo(""))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("Not found".getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_NOT_FOUND)));

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
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        wireMockServer.stubFor(get(urlPathEqualTo("/p/typo3/package-empty-result.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("".getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));

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
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));

        wireMockServer.stubFor(get(urlPathEqualTo("/p/typo3/package-empty-result.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("{}".getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));


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
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        wireMockServer.stubFor(head(urlPathEqualTo("/typo3/package-empty-result/-/typo3/package-empty-result-v1.2.0.tgz"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("".getBytes(),
                                new ContentTypeHeader("application/json")))
                        .withHeader("X-Checksum-MD5", "md5hash")
                        .withHeader("X-Checksum-SHA1", "sha1hash")
                        .withHeader("X-Checksum-SHA256", "sha256hash")
                        .withHeader("X-Checksum-SHA512", "sha512hash")
                        .withHeader("Last-Modified", "Thu, 07 Jul 2022 14:00:00 GMT")
                        .withStatus(HttpStatus.SC_OK)));

        var integrityMeta = analyzer.getIntegrityMeta(component);
        Assertions.assertNotNull(integrityMeta);
        assertThat(integrityMeta.getMetaSourceUrl()).contains("/typo3/package-empty-result/-/typo3/package-empty-result-v1.2.0.tgz");
        Assertions.assertEquals("md5hash", integrityMeta.getMd5());
        Assertions.assertEquals("sha1hash", integrityMeta.getSha1());
        Assertions.assertEquals("sha256hash", integrityMeta.getSha256());
        Assertions.assertEquals("sha512hash", integrityMeta.getSha512());
        Assertions.assertNotNull(integrityMeta.getCurrentVersionLastModified());
    }

    @Test
    void testIntegrityResultForPurlWithoutNamespace() {
        Component component = new Component();
        component.setPurl("pkg:npm/amazon-s3-uri@0.0.1");
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));

        wireMockServer.stubFor(head(urlPathEqualTo("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("".getBytes(),
                                new ContentTypeHeader("application/json")))
                        .withStatus(HttpStatus.SC_OK)));
        var integrityMeta = analyzer.getIntegrityMeta(component);
        Assertions.assertNotNull(integrityMeta);
        assertThat(integrityMeta.getMetaSourceUrl()).contains("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz");
    }

    @Test
    void testIntegrityAnalyzerException() {
        Component component = new Component();
        component.setPurl("pkg:npm/amazon-s3-uri@0.0.1");
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));

        wireMockServer.stubFor(head(urlPathEqualTo("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("".getBytes(),
                                new ContentTypeHeader("application/json")))
                        .withStatus(HttpStatus.SC_BAD_REQUEST)));
        var integrityMeta = analyzer.getIntegrityMeta(component);
        Assertions.assertNotNull(integrityMeta);
        assertThat(integrityMeta.getMetaSourceUrl()).contains("/amazon-s3-uri/-/amazon-s3-uri-0.0.1.tgz");
        Assertions.assertNull(integrityMeta.getSha1());
        Assertions.assertNull(integrityMeta.getMd5());
        Assertions.assertNull(integrityMeta.getSha256());
        Assertions.assertNull(integrityMeta.getSha512());
        Assertions.assertNull(integrityMeta.getCurrentVersionLastModified());
    }
}
