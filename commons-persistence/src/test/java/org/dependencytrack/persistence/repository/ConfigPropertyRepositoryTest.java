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
import org.dependencytrack.persistence.model.ConfigProperty;
import org.dependencytrack.persistence.model.ConfigPropertyConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ConfigPropertyRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    ConfigPropertyRepository repository;

    @Test
    @TestTransaction
    public void configProperty() {
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("ID", "DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    (1, 'Email address', 'email', 'STRING', 'smtp.from.address', 'abc@gmail.com');
                """).executeUpdate();

        final ConfigProperty config= repository
                .findByGroupAndName(ConfigPropertyConstants.EMAIL_SMTP_FROM_ADDR.getGroupName(), ConfigPropertyConstants.EMAIL_SMTP_FROM_ADDR.getPropertyName());
        Assertions.assertEquals("abc@gmail.com", config.getPropertyValue());
    }



}