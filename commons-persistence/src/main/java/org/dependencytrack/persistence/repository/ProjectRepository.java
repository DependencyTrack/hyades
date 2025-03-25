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

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.dependencytrack.persistence.model.Project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import java.util.UUID;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

/**
 * @since 0.6.0
 */
@ApplicationScoped
public class ProjectRepository implements PanacheRepository<Project> {

    public boolean isParentOfActiveChild(final Project parent, final UUID childUuid) {
        final Query query = getEntityManager().createNativeQuery("""
                SELECT EXISTS(
                  SELECT 1
                    FROM "PROJECT_HIERARCHY" AS hierarchy
                   INNER JOIN "PROJECT" AS child_project
                      ON child_project."ID" = hierarchy."CHILD_PROJECT_ID"
                   WHERE hierarchy."PARENT_PROJECT_ID" = :parentId
                     AND hierarchy."DEPTH" > 0
                     AND child_project."ID" = (SELECT "ID" FROM "PROJECT" WHERE "UUID" = :childUuid)
                     AND child_project."INACTIVE_SINCE" IS NULL
                )
                """);

        return (boolean) query
                .setParameter("parentId", parent.getId())
                .setParameter("childUuid", childUuid)
                .setHint(HINT_READ_ONLY, true)
                .getSingleResult();
    }

}
