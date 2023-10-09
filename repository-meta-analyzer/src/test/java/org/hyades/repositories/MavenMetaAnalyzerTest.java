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
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.impl.client.HttpClients;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

class MavenMetaAnalyzerTest {

    private IMetaAnalyzer analyzer;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void beforeEach() {
        analyzer = new MavenMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
    }

    @AfterEach
    void afterEach() {
        wireMock.resetAll();
    }

    @Test
    void testAnalyzer() throws Exception {
        Component component = new Component();
        component.setPurl(new PackageURL("pkg:maven/junit/junit@4.12"));
        Assertions.assertEquals("MavenMetaAnalyzer", analyzer.getName());
        Assertions.assertTrue(analyzer.isApplicable(component));
        Assertions.assertEquals(RepositoryType.MAVEN, analyzer.supportedRepositoryType());
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
    }

    @Test
    void testAnalyzerForScalaComponent() throws Exception {
        Component component = new Component();

        // Scala packages differ from others in that their name always includes the version of
        // the Scala compiler they were built with.
        component.setPurl(new PackageURL("pkg:maven/com.typesafe.akka/akka-actor_2.13@2.5.23"));
        Assertions.assertTrue(analyzer.isApplicable(component));
        Assertions.assertEquals(RepositoryType.MAVEN, analyzer.supportedRepositoryType());
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNotNull(metaModel.getLatestVersion());
        Assertions.assertNotNull(metaModel.getPublishedTimestamp());
    }

    @Test
    void testComponentWithNullPurl() {
        Component component = new Component();
        Assertions.assertFalse(analyzer.isApplicable(component));
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertNull(metaModel.getComponent().getPurl());
    }

    @Test
    void testComponentWithNonMavenPurl() {
        Component component = new Component();
        component.setPurl("pkg:pypi/com.typesafe.akka/package-does-not-exist@v1.2.0");
        Assertions.assertFalse(analyzer.isApplicable(component));
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertEquals(RepositoryType.PYPI.name(), metaModel.getComponent().getPurl().getType().toUpperCase());
    }

    @Test
    void testIOException() {
        Component component = new Component();
        component.setPurl("pkg:pypi/com.typesafe.akka/package-does-not-exist@v1.2.0");
        analyzer.setRepositoryBaseUrl("http://www.does.not.exist.com");
        MetaModel metaModel = analyzer.analyze(component);
        Assertions.assertEquals(metaModel.getComponent(), component);

    }

    @Test
    void testGetIntegrityMetaComponentNull() {
        var integrityModel = analyzer.getIntegrityMeta(null);
        Assertions.assertNull(integrityModel);
    }

    @Test
    void testGetIntegrityMeta404() {
        Component component = new Component();
        component.setPurl("pkg:maven/typo3/package-empty-result@v1.2.0");
        var integrityMeta = analyzer.getIntegrityMeta(component);
        assertNotNull(integrityMeta);
        assertEquals("https://repo1.maven.org/maven2/typo3/package-empty-result/v1.2.0/package-empty-result-v1.2.0.jar", integrityMeta.getMetaSourceUrl());
        assertNull(integrityMeta.getSha1());
        assertNull(integrityMeta.getMd5());
        assertNull(integrityMeta.getSha256());
        assertNull(integrityMeta.getSha512());
        assertNull(integrityMeta.getCurrentVersionLastModified());
    }

    @Test
    void testGetIntegrityModel200() {
        Component component = new Component();
        component.setPurl("pkg:maven/typo3/package-empty-result@v1.2.0");
        wireMock.stubFor(WireMock.head(WireMock.anyUrl()).withHeader("accept", containing("application/json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withResponseBody(Body.ofBinaryOrText("""
                                                                    
                                """.getBytes(), new ContentTypeHeader(MediaType.APPLICATION_JSON))
                        )
                        .withHeader("X-CheckSum-MD5", "md5hash")
                        .withHeader("X-Checksum-SHA1", "sha1hash")
                        .withHeader("X-Checksum-SHA512", "sha512hash")
                        .withHeader("X-Checksum-SHA256", "sha256hash")
                        .withHeader("Last-Modified", "Thu, 07 Jul 2022 14:00:00 GMT")));
        analyzer.setRepositoryBaseUrl(wireMock.baseUrl());
        var integrityMeta = analyzer.getIntegrityMeta(component);
        assertNotNull(integrityMeta);
        assertThat(integrityMeta.getMetaSourceUrl()).contains("/typo3/package-empty-result/v1.2.0/package-empty-result-v1.2.0.jar");
        assertEquals("md5hash", integrityMeta.getMd5());
        assertEquals("sha1hash", integrityMeta.getSha1());
        assertEquals("sha256hash", integrityMeta.getSha256());
        assertEquals("sha512hash", integrityMeta.getSha512());
        assertNotNull(integrityMeta.getCurrentVersionLastModified());
    }
}

