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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.dependencytrack.persistence.model.Team;

import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class TeamRepository implements PanacheRepository<Team> {

    private final EntityManager entityManager;

    @Inject
    TeamRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<Team> findByNotificationRule(final long notificationRuleId) {
        return entityManager
                .createNativeQuery("""
                        SELECT * FROM "TEAM" AS "T"
                            INNER JOIN "NOTIFICATIONRULE_TEAMS" AS "NT" ON "NT"."TEAM_ID" = "T"."ID"
                        WHERE "NT"."NOTIFICATIONRULE_ID" = :ruleId  
                        """, Team.class)
                .setParameter("ruleId", notificationRuleId)
                .setHint(HINT_READ_ONLY, true)
                .getResultList();
    }

}
