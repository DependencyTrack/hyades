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
package org.dependencytrack.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractDatabaseConfigSourceTest {

    private final DataSource dataSource;

    AbstractDatabaseConfigSourceTest(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    void createProperty(final String group, final String name, final String value) throws Exception {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("""
                     INSERT INTO "CONFIGPROPERTY" ("GROUPNAME", "PROPERTYNAME", "PROPERTYTYPE", "PROPERTYVALUE")
                     VALUES (?, ?, 'STRING', ?)
                     """)) {
            ps.setString(1, group);
            ps.setString(2, name);
            ps.setString(3, value);
            final int affectedRows = ps.executeUpdate();
            assertThat(affectedRows).isEqualTo(1);
        }
    }

    void deleteProperty(final String group, final String name) throws Exception {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("""
                     DELETE
                       FROM "CONFIGPROPERTY"
                      WHERE "GROUPNAME" = ?
                        AND "PROPERTYNAME" = ?;
                     """)) {
            ps.setString(1, group);
            ps.setString(2, name);
            final int affectedRows = ps.executeUpdate();
            assertThat(affectedRows).isEqualTo(1);
        }
    }

    void deleteAllProperties() throws Exception {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("""
                     DELETE
                       FROM "CONFIGPROPERTY"
                      WHERE "ID" > 0
                     """)) {
            ps.executeUpdate();
        }
    }

}
