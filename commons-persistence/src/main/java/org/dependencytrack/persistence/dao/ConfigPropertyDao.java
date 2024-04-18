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

import org.dependencytrack.persistence.model.ConfigProperty;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;

public interface ConfigPropertyDao extends SqlObject {

    @SqlQuery("""
            SELECT NULLIF(TRIM("PROPERTYVALUE"), '')
              FROM "CONFIGPROPERTY"
             WHERE "GROUPNAME" = :group
               AND "PROPERTYNAME" = :name
            """)
    Optional<String> getValue(@BindMethods ConfigProperty property);

    @SqlQuery("""
            SELECT "PROPERTYVALUE"::BOOLEAN
              FROM "CONFIGPROPERTY"
             WHERE "GROUPNAME" = :group
               AND "PROPERTYNAME" = :name
               AND "PROPERTYTYPE" = 'BOOLEAN'
            """)
    Optional<Boolean> isEnabled(@BindMethods ConfigProperty property);

}
