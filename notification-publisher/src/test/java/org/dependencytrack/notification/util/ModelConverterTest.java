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
package org.dependencytrack.notification.util;

import org.dependencytrack.persistence.model.NotificationGroup;
import org.dependencytrack.persistence.model.NotificationLevel;
import org.dependencytrack.proto.notification.v1.Group;
import org.dependencytrack.proto.notification.v1.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_UNSPECIFIED;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_UNSPECIFIED;

class ModelConverterTest {

    @ParameterizedTest
    @CsvSource(value = {
            "LEVEL_ERROR, ERROR",
            "LEVEL_WARNING, WARNING",
            "LEVEL_INFORMATIONAL, INFORMATIONAL"
    })
    void testConvertLevel(final Level given, final NotificationLevel expected) {
        assertThat(ModelConverter.convert(given)).isEqualTo(expected);
    }

    @Test
    void testConvertLevelUnknown() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ModelConverter.convert(LEVEL_UNSPECIFIED));
    }

    @Test
    void testConvertLevelNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ModelConverter.convert((Level) null));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "GROUP_CONFIGURATION, CONFIGURATION",
            "GROUP_DATASOURCE_MIRRORING, DATASOURCE_MIRRORING",
            "GROUP_REPOSITORY, REPOSITORY",
            "GROUP_INTEGRATION, INTEGRATION",
            "GROUP_FILE_SYSTEM, FILE_SYSTEM",
            "GROUP_ANALYZER, ANALYZER",
            "GROUP_NEW_VULNERABILITY, NEW_VULNERABILITY",
            "GROUP_NEW_VULNERABLE_DEPENDENCY, NEW_VULNERABLE_DEPENDENCY",
            "GROUP_PROJECT_AUDIT_CHANGE, PROJECT_AUDIT_CHANGE",
            "GROUP_BOM_CONSUMED, BOM_CONSUMED",
            "GROUP_BOM_PROCESSED, BOM_PROCESSED",
            "GROUP_VEX_CONSUMED, VEX_CONSUMED",
            "GROUP_VEX_PROCESSED, VEX_PROCESSED",
            "GROUP_POLICY_VIOLATION, POLICY_VIOLATION",
            "GROUP_PROJECT_CREATED, PROJECT_CREATED"
    })
    void testConvertGroup(final Group given, final NotificationGroup expected) {
        assertThat(ModelConverter.convert(given)).isEqualTo(expected);
    }

    @Test
    void testConvertGroupUnknown() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ModelConverter.convert(GROUP_UNSPECIFIED));
    }

    @Test
    void testConvertGroupNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ModelConverter.convert((Group) null));
    }

}