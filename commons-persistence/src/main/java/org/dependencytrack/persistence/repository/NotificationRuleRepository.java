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
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.dependencytrack.persistence.model.NotificationLevel;
import org.dependencytrack.persistence.model.NotificationRule;
import org.dependencytrack.persistence.model.NotificationScope;

import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class NotificationRuleRepository implements PanacheRepository<NotificationRule> {

    public List<NotificationRule> findEnabledByScopeAndForLevel(final NotificationScope scope, final NotificationLevel level) {
        return switch (level) {
            case INFORMATIONAL -> find("enabled and scope = :scope and notificationLevel = :level",
                    Parameters.with("scope", scope)
                            .and("level", level))
                    .withHint(HINT_READ_ONLY, true)
                    .list();
            case WARNING ->
                    find("enabled and scope = :scope and (notificationLevel = :levelWarn or notificationLevel = :levelInfo)",
                            Parameters.with("scope", scope)
                                    .and("levelWarn", level)
                                    .and("levelInfo", NotificationLevel.INFORMATIONAL))
                            .withHint(HINT_READ_ONLY, true)
                            .list();
            case ERROR -> find("""
                           enabled and scope = :scope and (
                                notificationLevel = :levelErr or
                                notificationLevel = :levelWarn or
                                notificationLevel = :levelInfo
                            )
                            """,
                    Parameters.with("scope", scope)
                            .and("levelErr", level)
                            .and("levelWarn", NotificationLevel.WARNING)
                            .and("levelInfo", NotificationLevel.INFORMATIONAL))
                    .withHint(HINT_READ_ONLY, true)
                    .list();
        };
    }

}
