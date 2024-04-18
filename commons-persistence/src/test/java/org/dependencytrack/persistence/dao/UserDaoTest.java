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
import jakarta.inject.Inject;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class UserDaoTest {

    @Inject
    Jdbi jdbi;

    private Handle jdbiHandle;
    private UserDao userDao;

    @BeforeEach
    void beforeEach() {
        jdbiHandle = jdbi.open();
        userDao = jdbiHandle.attach(UserDao.class);
    }

    @AfterEach
    void afterEach() {
        if (jdbiHandle != null) {
            jdbiHandle.close();
        }
    }

    @Test
    void testGetEmailsByTeamIdAnyOf() {
        final List<Long> teamIds = jdbiHandle.createUpdate("""
                        INSERT INTO "TEAM" ("NAME", "UUID")
                        VALUES ('foo', 'ba38e779-e252-4033-8e76-156dc46cc7a6')
                             , ('bar', '507d8f3c-431d-47aa-929e-7647746d07a9')
                        RETURNING "ID"
                        """)
                .executeAndReturnGeneratedKeys("ID")
                .mapTo(Long.class)
                .list();
        final Long teamFooId = teamIds.get(0);
        final Long teamBarId = teamIds.get(1);

        final List<Long> managedUserIds = jdbiHandle.createUpdate("""
                        INSERT INTO "MANAGEDUSER" ("EMAIL", "PASSWORD", "FORCE_PASSWORD_CHANGE", "LAST_PASSWORD_CHANGE", "NON_EXPIRY_PASSWORD", "SUSPENDED")
                        VALUES ('foo@managed.example.com', 'foo', FALSE, NOW(), TRUE, FALSE)
                             , ('bar@managed.example.com', 'bar', FALSE, NOW(), TRUE, FALSE)
                             , ('baz@managed.example.com', 'baz', FALSE, NOW(), TRUE, FALSE)
                             , (NULL, 'qux', FALSE, NOW(), TRUE, FALSE)
                             , ('quux@example.com', 'quux', FALSE, NOW(), TRUE, FALSE)
                        RETURNING "ID"
                        """)
                .executeAndReturnGeneratedKeys("ID")
                .mapTo(Long.class)
                .list();
        final Long managedUserFooId = managedUserIds.get(0);
        final Long managedUserBarId = managedUserIds.get(1);
        final Long managedUserQuxId = managedUserIds.get(3);
        final Long managedUserQuuxId = managedUserIds.get(4);

        jdbiHandle.createUpdate("""
                        INSERT INTO "MANAGEDUSERS_TEAMS" ("MANAGEDUSER_ID", "TEAM_ID")
                        VALUES (:userFooId, :teamFooId)
                             , (:userBarId, :teamFooId)
                             , (:userQuxId, :teamFooId)
                             , (:userBarId, :teamBarId)
                             , (:userQuuxId, :teamBarId)
                        """)
                .bind("userFooId", managedUserFooId)
                .bind("userBarId", managedUserBarId)
                .bind("userQuxId", managedUserQuxId)
                .bind("userQuuxId", managedUserQuuxId)
                .bind("teamFooId", teamFooId)
                .bind("teamBarId", teamBarId)
                .execute();

        final List<Long> ldapUserIds = jdbiHandle.createUpdate("""
                        INSERT INTO "LDAPUSER" ("EMAIL", "DN")
                        VALUES ('foo@ldap.example.com', 'foo')
                             , ('bar@ldap.example.com', 'bar')
                             , ('baz@ldap.example.com', 'baz')
                             , (NULL, 'qux')
                             , ('quux@example.com', 'quux')
                        RETURNING "ID"
                        """)
                .executeAndReturnGeneratedKeys("ID")
                .mapTo(Long.class)
                .list();
        final Long ldapUserFooId = ldapUserIds.get(0);
        final Long ldapUserBarId = ldapUserIds.get(1);
        final Long ldapUserQuxId = ldapUserIds.get(3);
        final Long ldapUserQuuxId = ldapUserIds.get(4);

        jdbiHandle.createUpdate("""
                        INSERT INTO "LDAPUSERS_TEAMS" ("LDAPUSER_ID", "TEAM_ID")
                        VALUES (:userFooId, :teamFooId)
                             , (:userBarId, :teamFooId)
                             , (:userQuxId, :teamFooId)
                             , (:userBarId, :teamBarId)
                             , (:userQuuxId, :teamBarId)
                        """)
                .bind("userFooId", ldapUserFooId)
                .bind("userBarId", ldapUserBarId)
                .bind("userQuxId", ldapUserQuxId)
                .bind("userQuuxId", ldapUserQuuxId)
                .bind("teamFooId", teamFooId)
                .bind("teamBarId", teamBarId)
                .execute();

        final List<Long> oidcUserIds = jdbiHandle.createUpdate("""
                        INSERT INTO "OIDCUSER" ("EMAIL", "USERNAME")
                        VALUES ('foo@oidc.example.com', 'foo')
                             , ('bar@oidc.example.com', 'bar')
                             , ('baz@oidc.example.com', 'baz')
                             , (NULL, 'qux')
                             , ('quux@example.com', 'quux')
                        RETURNING "ID"
                        """)
                .executeAndReturnGeneratedKeys("ID")
                .mapTo(Long.class)
                .list();
        final Long oidcUserFooId = oidcUserIds.get(0);
        final Long oidcUserBarId = oidcUserIds.get(1);
        final Long oidcUserQuxId = oidcUserIds.get(3);
        final Long oidcUserQuuxId = oidcUserIds.get(4);

        jdbiHandle.createUpdate("""
                        INSERT INTO "OIDCUSERS_TEAMS" ("OIDCUSERS_ID", "TEAM_ID")
                        VALUES (:userFooId, :teamFooId)
                             , (:userBarId, :teamFooId)
                             , (:userQuxId, :teamFooId)
                             , (:userBarId, :teamBarId)
                             , (:userQuuxId, :teamBarId)
                        """)
                .bind("userFooId", oidcUserFooId)
                .bind("userBarId", oidcUserBarId)
                .bind("userQuxId", oidcUserQuxId)
                .bind("userQuuxId", oidcUserQuuxId)
                .bind("teamFooId", teamFooId)
                .bind("teamBarId", teamBarId)
                .execute();

        assertThat(userDao.getEmailsByTeamIdAnyOf(List.of(teamFooId, teamBarId)))
                .hasEntrySatisfying(teamFooId, emails -> assertThat(emails).containsExactlyInAnyOrder(
                        "foo@managed.example.com",
                        "bar@managed.example.com",
                        "foo@ldap.example.com",
                        "bar@ldap.example.com",
                        "foo@oidc.example.com",
                        "bar@oidc.example.com"
                ))
                .hasEntrySatisfying(teamBarId, emails -> assertThat(emails).containsExactlyInAnyOrder(
                        "bar@managed.example.com",
                        "bar@ldap.example.com",
                        "bar@oidc.example.com",
                        "quux@example.com"
                        // Results are de-duplicated, thus quux@example.com must not appear more than
                        // once, despite multiple users having that email.
                ));
    }

    @Test
    void testGetEmailsByTeamIdAnyOfWithoutIds() {
        assertThat(userDao.getEmailsByTeamIdAnyOf(Collections.emptyList())).isEmpty();
    }

}