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
import org.dependencytrack.persistence.model.NotificationLevel;
import org.dependencytrack.persistence.model.NotificationRule;
import org.dependencytrack.persistence.model.NotificationScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
class NotificationRuleRepositoryTest {

    @Inject
    EntityManager entityManager;

    @Inject
    NotificationRuleRepository repository;

    @Test
    @TestTransaction
    public void testRuleLevelEqual() {
        entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "NOTIFY_ON", "NOTIFY_CHILDREN", "LOG_SUCCESSFUL_PUBLISH", "NOTIFICATION_LEVEL", "SCOPE", "UUID") VALUES
                                    (true, 'foo', 'NEW_VULNERABILITY', false, true, 'WARNING', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62');
                """).executeUpdate();

        final List<NotificationRule> rules = repository
                .findEnabledByScopeAndForLevel(NotificationScope.PORTFOLIO, NotificationLevel.WARNING);
        Assertions.assertEquals(1, rules.size());
    }

    @Test
    @TestTransaction
    public void testRuleLevelBelow() {
        entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "NOTIFY_ON", "NOTIFY_CHILDREN", "LOG_SUCCESSFUL_PUBLISH", "NOTIFICATION_LEVEL", "SCOPE", "UUID") VALUES
                                    (true, 'foo', 'NEW_VULNERABILITY', false, true, 'WARNING', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62');
                """).executeUpdate();

        final List<NotificationRule> rules = repository
                .findEnabledByScopeAndForLevel(NotificationScope.PORTFOLIO, NotificationLevel.ERROR);
        Assertions.assertEquals(1, rules.size());
    }

    @Test
    @TestTransaction
    public void testRuleLevelAbove() {
        entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "NOTIFY_ON", "NOTIFY_CHILDREN", "LOG_SUCCESSFUL_PUBLISH", "NOTIFICATION_LEVEL", "SCOPE", "UUID") VALUES
                                    (true, 'foo', 'NEW_VULNERABILITY', false, true, 'WARNING', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62');
                """).executeUpdate();

        final List<NotificationRule> rules = repository
                .findEnabledByScopeAndForLevel(NotificationScope.PORTFOLIO, NotificationLevel.INFORMATIONAL);
        Assertions.assertEquals(0, rules.size());
    }

}