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
import org.apache.http.impl.client.HttpClients;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MavenMetaAnalyzerTest {

    private IMetaAnalyzer analyzer;

    @BeforeEach
    void beforeEach() {
        analyzer = new MavenMetaAnalyzer();
        analyzer.setHttpClient(HttpClients.createDefault());
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
    void testInvalidPurl(){

    }
}

