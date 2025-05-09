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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class UserRepository {

    private final EntityManager entityManager;

    @Inject
    UserRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<String> findEmailsByTeam(final long teamId) {
        return entityManager.createNativeQuery("""
                        SELECT DISTINCT "USER"."EMAIL"
                          FROM "USERS_TEAMS"
                         INNER JOIN "USER"
                            ON "USER"."ID" = "USERS_TEAMS"."USER_ID"
                         WHERE "TEAM_ID" = :teamId
                           AND "USER"."EMAIL" IS NOT NULL
                        """)
                .setParameter("teamId", teamId)
                .setHint(HINT_READ_ONLY, true)
                .getResultList();
    }

}
