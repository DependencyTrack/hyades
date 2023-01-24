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
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@QuarkusTest
class ComposerMetaAnalyzerTest {
    private static ClientAndServer mockServer;
    @Inject
    ComposerMetaAnalyzer analyzer;

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

        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", mockServer.getPort()));
        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/p/typo3/class-alias-loader.json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withBody(getTestData(packagistFile))
                );

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
        component.setPurl(new PackageURL("pkg:composer/typo3/package-empty-result@v1.2.0"));
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
        component.setPurl(new PackageURL("pkg:composer/typo3/package-empty-result@v1.2.0"));
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

    private static File getResourceFile(String namespace, String name) throws Exception {
        return new File(
                Thread.currentThread().getContextClassLoader()
                        .getResource(String.format(
                                "unit/repositories/https---repo.packagist.org-p-%s-%s.json",
                                namespace,
                                name
                        ))
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
