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
package org.dependencytrack.persistence.dao;

import io.quarkus.test.junit.QuarkusTest;
import org.dependencytrack.persistence.model.Team;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.List;

@QuarkusTest
class NotificationDaoTest {

    @Inject
    Jdbi jdbi;

    private Handle jdbiHandle;
    private NotificationDao notificationDao;

    @BeforeEach
    void beforeEach() {
        jdbiHandle = jdbi.open();
        notificationDao = jdbiHandle.attach(NotificationDao.class);
    }

    @AfterEach
    void afterEach() {
        if (jdbiHandle != null) {
            jdbiHandle.close();
        }
    }

    @Test
    void testGetTeamsByRuleId() {
        final List<Long> teamIds = jdbiHandle.createUpdate("""
                        INSERT INTO "TEAM" ("NAME", "UUID")
                        VALUES ('foo', 'fa26b29f-e106-4d62-b1a3-2073b63c9dd0')
                             , ('bar', 'c18d0094-f161-4581-96fa-bfc7e413c78d')
                             , ('baz', '6db9c0cb-9c84-440a-89a8-9bbed5d028d9')
                        RETURNING "ID"
                        """)
                .executeAndReturnGeneratedKeys("ID")
                .mapTo(Long.class)
                .list();
        final Long teamFooId = teamIds.get(0);
        final Long teamBarId = teamIds.get(1);

        final List<Long> ruleIds = jdbiHandle.createUpdate("""
                        INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "NOTIFY_CHILDREN", "SCOPE", "UUID")
                        VALUES (true, 'foo', false, 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62')
                             , (true, 'bar', false, 'PORTFOLIO', 'ee74dc70-cd8e-41df-ae6a-1093d5f7b608')
                        RETURNING "ID"
                        """)
                .executeAndReturnGeneratedKeys("ID")
                .mapTo(Long.class)
                .list();
        final Long ruleFooId = ruleIds.get(0);

        jdbiHandle.createUpdate("""                            
                        INSERT INTO "NOTIFICATIONRULE_TEAMS" ("NOTIFICATIONRULE_ID", "TEAM_ID")
                        VALUES (:ruleFooId, :teamFooId)
                             , (:ruleFooId, :teamBarId)
                        """)
                .bind("ruleFooId", ruleFooId)
                .bind("teamFooId", teamFooId)
                .bind("teamBarId", teamBarId)
                .execute();

        final List<Team> teams = notificationDao.getTeamsByRuleId(ruleFooId);
        Assertions.assertEquals(2, teams.size());
        Assertions.assertEquals("foo", teams.get(0).name());
        Assertions.assertEquals("bar", teams.get(1).name());

        Assertions.assertEquals(0, notificationDao.getTeamsByRuleId(2).size());
    }

}