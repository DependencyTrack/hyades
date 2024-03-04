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

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

class ComposerMetaAnalyzerTest {
    static WireMockServer wireMockServer;

    @AfterEach
    void afterEach() {
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

    @BeforeEach
    void beforeEach() {
        analyzer = new ComposerMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();
    }

    private IMetaAnalyzer analyzer;


    @Test
    void testAnalyzer() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:composer/phpunit/phpunit@1.0.0"));
        Assertions.assertEquals("ComposerMetaAnalyzer", analyzer.getName());
        Assertions.assertTrue(analyzer.isApplicable(component));
        Assertions.assertEquals(RepositoryType.COMPOSER, analyzer.supportedRepositoryType());
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
    }

    @Test
    void testAnalyzerFindsVersionWithLeadingV() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:composer/typo3/class-alias-loader@v1.1.0"));
        final File packagistFile = getResourceFile("typo3", "class-alias-loader");

        wireMockServer.stubFor(get(urlPathEqualTo("/p/typo3/class-alias-loader.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withResponseBody(Body.ofBinaryOrText(getTestData(packagistFile),
                                new ContentTypeHeader(MediaType.APPLICATION_JSON))).withStatus(HttpStatus.SC_OK)));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertEquals("v1.1.3", metaModel.getLatestVersion());
        Assertions.assertEquals(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss XXX").parse("2020-05-24 13:03:22 Z"),
                metaModel.getPublishedTimestamp()
        );
    }

    @Test
    void testAnalyzerDoesNotFindResult() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:composer/typo3/package-does-not-exist@v1.2.0"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        wireMockServer.stubFor(get(urlPathEqualTo(""))
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
        component.setPurl(new PackageURL("pkg:composer/typo3/package-empty-result@v1.2.0"));
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
        component.setPurl(new PackageURL("pkg:composer/typo3/package-empty-result@v1.2.0"));
        wireMockServer.stubFor(get(urlPathEqualTo("/p/typo3/package-empty-result.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withResponseBody(Body.ofBinaryOrText("{}".getBytes(),
                                new ContentTypeHeader(MediaType.APPLICATION_JSON))).withStatus(HttpStatus.SC_OK)));

        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertNull(metaModel.getLatestVersion());
        Assertions.assertNull(
                metaModel.getPublishedTimestamp()
        );
    }

    @Test
    void testAnalyzerGetsUnexpectedResponseContentCausingLatestVersionBeingNull() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:composer/magento/adobe-ims@v1.0.0"));
        final File packagistFile = getResourceFile("magento", "adobe-ims");

        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        wireMockServer.stubFor(get(urlPathEqualTo("/p/magento/adobe-ims.json"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withResponseBody(Body.ofBinaryOrText(getTestData(packagistFile),
                                new ContentTypeHeader(MediaType.APPLICATION_JSON))).withStatus(HttpStatus.SC_OK)));


        MetaModel metaModel = analyzer.analyze(component);

        Assertions.assertNull(metaModel.getLatestVersion());
    }


    private static File getResourceFile(String namespace, String name) throws Exception {
        return new File(
                Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                                .getResource(String.format(
                                        "unit/repositories/https---repo.packagist.org-p-%s-%s.json",
                                        namespace,
                                        name
                                )))
                        .toURI()
        );
    }

    private static byte[] getTestData(File file) throws Exception {
        final FileInputStream fileStream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fileStream.read(data);
        fileStream.close();
        return data;
    }
}
