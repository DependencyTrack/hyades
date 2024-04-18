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

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface UserDao {

    @SqlQuery("""
            SELECT "MU"."EMAIL" AS "EMAIL"
                 , "MUT"."TEAM_ID" AS "TEAM_ID"
              FROM "MANAGEDUSER" AS "MU"
             INNER JOIN "MANAGEDUSERS_TEAMS" AS "MUT"
                ON "MUT"."MANAGEDUSER_ID" = "MU"."ID"
             WHERE "MUT"."TEAM_ID" = ANY(:teamIds)
               AND "MU"."EMAIL" IS NOT NULL
             UNION
            SELECT "LU"."EMAIL" AS "EMAIL"
                 , "LUT"."TEAM_ID" AS "TEAM_ID"
              FROM "LDAPUSER" AS "LU"
             INNER JOIN "LDAPUSERS_TEAMS" AS "LUT"
                ON "LUT"."LDAPUSER_ID" = "LU"."ID"
             WHERE "LUT"."TEAM_ID" = ANY(:teamIds)
               AND "LU"."EMAIL" IS NOT NULL
             UNION
            SELECT "OU"."EMAIL" AS "EMAIL"
                 , "OUT"."TEAM_ID" AS "TEAM_ID"
              FROM "OIDCUSER" AS "OU"
             INNER JOIN "OIDCUSERS_TEAMS" AS "OUT"
                ON "OUT"."OIDCUSERS_ID" = "OU"."ID"
             WHERE "OUT"."TEAM_ID" = ANY(:teamIds)
               AND "OU"."EMAIL" IS NOT NULL
            """)
    @UseRowReducer(EmailsByTeamIdResultRowReducer.class)
    Map<Long, Set<String>> getEmailsByTeamIdAnyOf(@Bind Collection<Long> teamIds);

    class EmailsByTeamIdResultRowReducer implements RowReducer<Map<Long, Set<String>>, Map.Entry<Long, Set<String>>> {

        @Override
        public Map<Long, Set<String>> container() {
            return new HashMap<>();
        }

        @Override
        public void accumulate(final Map<Long, Set<String>> container, final RowView rowView) {
            container.compute(rowView.getColumn("TEAM_ID", Long.class), (teamId, emails) -> {
                final Set<String> mutableEmails = emails == null ? new HashSet<>() : emails;
                mutableEmails.add(rowView.getColumn("EMAIL", String.class));
                return mutableEmails;
            });
        }

        @Override
        public Stream<Map.Entry<Long, Set<String>>> stream(final Map<Long, Set<String>> container) {
            return container.entrySet().stream();
        }

    }

}
