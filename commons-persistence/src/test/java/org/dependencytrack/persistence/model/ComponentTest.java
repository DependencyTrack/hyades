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
package org.dependencytrack.persistence.model;

import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ComponentTest {

    @Test
    public void testId() {
        Component component = new Component();
        component.setId(111);
        Assertions.assertEquals(111, component.getId());
    }

    @Test
    public void testGroup() {
        Component component = new Component();
        component.setGroup("group");
        Assertions.assertEquals("group", component.getGroup());
    }

    @Test
    public void testName() {
        Component component = new Component();
        component.setName("name");
        Assertions.assertEquals("name", component.getName());
    }

    @Test
    public void testVersion() {
        Component component = new Component();
        component.setVersion("1.0");
        Assertions.assertEquals("1.0", component.getVersion());
    }

    @Test
    public void testFilename() {
        Component component = new Component();
        component.setFilename("foo.bar");
        Assertions.assertEquals("foo.bar", component.getFilename());
    }

    @Test
    public void testMd5() {
        Component component = new Component();
        String hash = "299189766eddf8b5fea4954f0a63d4b1";
        component.setMd5(hash);
        Assertions.assertEquals(hash, component.getMd5());
    }

    @Test
    public void testSha1() {
        Component component = new Component();
        String hash = "74f7fcc24e02e61b0eb367e273139b6b24c6587f";
        component.setSha1(hash);
        Assertions.assertEquals(hash, component.getSha1());
    }

    @Test
    public void testSha256()  {
        Component component = new Component();
        String hash = "cfb16d5a50169bac7699d6fc1ad4f8f2559d09e3fa580003b149ae0134e16d05";
        component.setSha256(hash);
        Assertions.assertEquals(hash, component.getSha256());
    }

    @Test
    public void testSha512() {
        Component component = new Component();
        String hash = "d52e762d8e1b8a33c7f7b4b2ab356a02d43e6bf51d273a5809a3478dc47f17b6df350890d06bb0240a7d3f51f49dde564a32f569952c8b02f54242cc3f92d277";
        component.setSha512(hash);
        Assertions.assertEquals(hash, component.getSha512());
    }


    @Test
    public void testPurl() throws Exception {
        Component component = new Component();
        PackageURL purl = PackageURLBuilder.aPackageURL()
                .withType("maven").withNamespace("acme").withName("product").withVersion("1.0").build();
        component.setPurl(purl);
        Assertions.assertEquals(purl.toString(), component.getPurl().toString());
    }

    @Test
    public void testDescription() {
        Component component = new Component();
        component.setDescription("Component description");
        Assertions.assertEquals("Component description", component.getDescription());
    }

    @Test
    public void testCopyright() {
        Component component = new Component();
        component.setCopyright("Copyright Acme");
        Assertions.assertEquals("Copyright Acme", component.getCopyright());
    }

    @Test
    public void testLicense() {
        Component component = new Component();
        component.setLicense("Apache 2.0");
        Assertions.assertEquals("Apache 2.0", component.getLicense());
    }


    @Test
    public void testUuid() {
        UUID uuid = UUID.randomUUID();
        Component component = new Component();
        component.setUuid(uuid);
        Assertions.assertEquals(uuid.toString(), component.getUuid().toString());
    }

    @Test
    public void testToStringWithPurl() throws Exception {
        Component component = new Component();
        PackageURL purl = PackageURLBuilder.aPackageURL()
                .withType("maven").withNamespace("acme").withName("product").withVersion("1.0").build();
        component.setPurl(purl);
        Assertions.assertEquals(component.getPurl().toString(), component.toString());
    }

    @Test
    public void testToStringWithoutPurl() {
        Component component = new Component();
        component.setGroup("acme");
        component.setName("product");
        component.setVersion("1.0");
        Assertions.assertEquals("acme : product : 1.0", component.toString());
    }
}
