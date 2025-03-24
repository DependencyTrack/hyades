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
import org.dependencytrack.persistence.model.Project;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ProjectRepositoryTest {

    @Inject
    ProjectRepository projectRepository;

    @Test
    @TestTransaction
    void testIsParentOfActiveChild() {
        final var parentProject = new Project();
        parentProject.setId(1);
        parentProject.setUuid(UUID.fromString("adbf4d72-6ebc-429a-955b-265a8b8ba997"));
        parentProject.setName("acme-app-parent");
        projectRepository.persist(parentProject);

        final var childProjectA = new Project();
        childProjectA.setId(2);
        childProjectA.setUuid(UUID.fromString("970829b1-3112-42db-a5ab-73391463e349"));
        childProjectA.setParent(parentProject);
        childProjectA.setName("acme-app-child-a");
        projectRepository.persist(childProjectA);

        final var childProjectB = new Project();
        childProjectB.setId(3);
        childProjectB.setUuid(UUID.fromString("bbf9e846-cc5a-493b-bc9a-ce944795a5ad"));
        childProjectB.setParent(parentProject);
        childProjectB.setName("acme-app-child-b");
        childProjectB.setInactiveSince(new Date());
        projectRepository.persist(childProjectB);

        assertThat(projectRepository.isParentOfActiveChild(parentProject, childProjectA.getUuid())).isTrue();
        assertThat(projectRepository.isParentOfActiveChild(parentProject, childProjectB.getUuid())).isFalse();
        assertThat(projectRepository.isParentOfActiveChild(parentProject, parentProject.getUuid())).isFalse();
    }

}