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
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

public class NixpkgsMetaAnalyzerTest {

    private IMetaAnalyzer analyzer;

    static WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() {
        analyzer = new NixpkgsMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
        wireMockServer = new WireMockServer(1080);
        wireMockServer.start();
    }

    @AfterEach
    void afterEach() {
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

    @Test
    public void testAnalyzerWithEmptyPackageResponse() throws Exception {
        final var component = new Component();
        component.setPurl(new PackageURL("pkg:nixpkgs/SDL_sound@1.0.3"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("""
                                {
                                    "packages": {}
                                }
                                """.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
        Assert.assertTrue(analyzer.isApplicable(component));
        Assert.assertEquals(RepositoryType.NIXPKGS, analyzer.supportedRepositoryType());
        Assert.assertNotNull(analyzer.analyze(component).getComponent());
    }

    @Test
    public void testAnalyzerWithPackageResponse() throws Exception {
        final var component = new Component();
        component.setPurl(new PackageURL("pkg:nixpkgs/SDL_sound@1.0.3"));
        analyzer.setRepositoryBaseUrl(String.format("http://localhost:%d", wireMockServer.port()));
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withResponseBody(Body.ofBinaryOrText("""
                                {
                                    "packages": {
                                        "p1": {
                                            "pname": "SDL_sound",
                                            "version": "1.0.5"
                                        }
                                    }
                                }
                                """.getBytes(),
                                new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
        Assert.assertTrue(analyzer.isApplicable(component));
        Assert.assertEquals(RepositoryType.NIXPKGS, analyzer.supportedRepositoryType());
        var metaModel = analyzer.analyze(component);
        Assert.assertNotNull(metaModel.getComponent());
        Assert.assertEquals("1.0.5", metaModel.getLatestVersion());
    }
}
