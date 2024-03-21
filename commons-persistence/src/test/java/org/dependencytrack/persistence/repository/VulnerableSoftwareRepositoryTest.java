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
package org.dependencytrack.persistence.repository;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.dependencytrack.persistence.model.Component;
import org.dependencytrack.persistence.model.VulnerableSoftware;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@QuarkusTest
public class VulnerableSoftwareRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    VulnerableSoftwareRepository vulnerableSoftwareRepository;

    @Test
    @TestTransaction
    public void testGetAllVulnerableSoftwareForPurl() {
        UUID uuid = UUID.randomUUID();
        var component = new Component();
        component.setName("github.com/tidwall/gjson");
        component.setVersion("v1.6.0");
        component.setPurl("pkg:golang/github.com/tidwall/gjson@v1.6.0?type=module");
        component.setUuid(uuid);

        entityManager.createNativeQuery("""
                INSERT INTO "VULNERABLESOFTWARE" ("ID", "UUID","PURL_TYPE", "PURL_NAMESPACE", "PURL_NAME", "VERSIONENDEXCLUDING", "VULNERABLE") VALUES
                                    (2, :uuid,'golang', 'github.com/tidwall', 'gjson', '1.6.5', true);
                """).setParameter("uuid", uuid).executeUpdate();

        List<VulnerableSoftware> vsList = vulnerableSoftwareRepository.getAllVulnerableSoftware(null, null, null, component.getPurl());
        Assertions.assertEquals("golang", vsList.get(0).getPurlType());
    }

    @Test
    @TestTransaction
    public void testGetAllVulnerableSoftwareForPurlWithameSpaceNull() {
        UUID uuid = UUID.randomUUID();
        var component = new Component();
        component.setName("django@1.11.1");
        component.setVersion("v1.6.0");
        component.setPurl("pkg:pypi/django@1.11.1");
        component.setUuid(uuid);

        entityManager.createNativeQuery("""
                INSERT INTO "VULNERABLESOFTWARE" ("ID", "UUID","PURL_TYPE", "PURL_NAMESPACE", "PURL_NAME", "VERSIONENDEXCLUDING", "VULNERABLE") VALUES
                                    (2, :uuid,'pypi', null, 'django', '1.11.1', true);
                """).setParameter("uuid", uuid).executeUpdate();

        List<VulnerableSoftware> vsList = vulnerableSoftwareRepository.getAllVulnerableSoftware(null, null, null, component.getPurl());
        Assertions.assertEquals("pypi", vsList.get(0).getPurlType());
    }

    @Test
    @TestTransaction
    public void testGetAllVulnerableSoftwareForCpe() {
        UUID uuid = UUID.randomUUID();
        var component = new Component();
        component.setName("github.com/tidwall/gjson");
        component.setVersion("v1.6.0");
        component.setUuid(uuid);
        component.setCpe("cpe:/a:acme:application:1.0.0");

        entityManager.createNativeQuery("""
                INSERT INTO "VULNERABLESOFTWARE" ("ID", "UUID","PART", "PRODUCT", "VENDOR", "VERSIONENDEXCLUDING", "VULNERABLE") VALUES
                                    (3, :uuid,'a','application', 'acme', '1.6.5', true);
                """).setParameter("uuid", uuid).executeUpdate();

        List<VulnerableSoftware> vsList = vulnerableSoftwareRepository.getAllVulnerableSoftware("a", "acme", "application", null);

        Assertions.assertEquals(uuid, vsList.get(0).getUuid());

    }

    @Test
    @TestTransaction
    public void testGetAllVulnerableSoftwareForCpeAndPurl() {
        UUID uuid = UUID.randomUUID();
        var component = new Component();
        component.setName("github.com/tidwall/gjson");
        component.setVersion("v1.6.0");
        component.setUuid(uuid);
        component.setCpe("cpe:/a:acme:application:1.0.0");
        component.setPurl("pkg:golang/github.com/tidwall/gjson@v1.6.0?type=module");
        entityManager.createNativeQuery("""
                INSERT INTO "VULNERABLESOFTWARE" ("ID", "UUID","PART", "PRODUCT", "VENDOR", "VERSIONENDEXCLUDING", "VULNERABLE","PURL_TYPE", "PURL_NAMESPACE", "PURL_NAME") VALUES
                                    (3, :uuid,'a','application', 'acme', '1.6.5', true,'golang', 'github.com/tidwall', 'gjson');
                """).setParameter("uuid", uuid).executeUpdate();

        List<VulnerableSoftware> vsList = vulnerableSoftwareRepository.getAllVulnerableSoftware("a", "acme", "application", component.getPurl());
        Assertions.assertEquals(uuid, vsList.get(0).getUuid());
    }

    @Test
    @TestTransaction
    public void testEmptyList() {
        List<VulnerableSoftware> vsList = vulnerableSoftwareRepository.getAllVulnerableSoftware(null, null, null, null);
        Assertions.assertEquals(0, vsList.size());
    }

}