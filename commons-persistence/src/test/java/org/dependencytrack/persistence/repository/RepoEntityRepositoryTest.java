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
import org.dependencytrack.persistence.model.Repository;
import org.dependencytrack.persistence.model.RepositoryType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
class RepoEntityRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    RepoEntityRepository repository;

    @Test
    @TestTransaction
    public void configProperty() {

        entityManager.createNativeQuery("""
                INSERT INTO "REPOSITORY" ("ID", "ENABLED", "IDENTIFIER", "INTERNAL", "PASSWORD", "RESOLUTION_ORDER", "TYPE", "URL") VALUES
                                    (1, 'true', 'central', 'false', 'null', 2, 'MAVEN', 'https://repo1.maven.org/maven2/');
                """).executeUpdate();
        entityManager.createNativeQuery("""
                INSERT INTO "REPOSITORY" ("ID", "ENABLED", "IDENTIFIER", "INTERNAL", "PASSWORD", "RESOLUTION_ORDER", "TYPE", "URL") VALUES
                                    (2, 'true', 'central2', 'false', 'null', 1, 'MAVEN', 'https://repo1.maven.org/maven2/123');
                """).executeUpdate();
        final List<Repository> config= repository
                .findEnabledRepositoriesByType(RepositoryType.MAVEN);
        Assertions.assertEquals(2, config.size());
        Assertions.assertEquals(1, config.get(0).getResolutionOrder());
        Assertions.assertEquals(2, config.get(1).getResolutionOrder());
    }



}