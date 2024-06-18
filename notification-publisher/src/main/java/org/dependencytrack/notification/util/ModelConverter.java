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

public final class ModelConverter {

    private ModelConverter() {
    }

    public static NotificationLevel convert(final Level level) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }

        return switch (level) {
            case LEVEL_ERROR -> NotificationLevel.ERROR;
            case LEVEL_WARNING -> NotificationLevel.WARNING;
            case LEVEL_INFORMATIONAL -> NotificationLevel.INFORMATIONAL;
            default -> throw new IllegalArgumentException("Unknown level: " + level);
        };
    }

    public static NotificationGroup convert(final Group group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }

        return switch (group) {
            case GROUP_CONFIGURATION -> NotificationGroup.CONFIGURATION;
            case GROUP_DATASOURCE_MIRRORING -> NotificationGroup.DATASOURCE_MIRRORING;
            case GROUP_REPOSITORY -> NotificationGroup.REPOSITORY;
            case GROUP_INTEGRATION -> NotificationGroup.INTEGRATION;
            case GROUP_FILE_SYSTEM -> NotificationGroup.FILE_SYSTEM;
            case GROUP_ANALYZER -> NotificationGroup.ANALYZER;
            case GROUP_NEW_VULNERABILITY -> NotificationGroup.NEW_VULNERABILITY;
            case GROUP_NEW_VULNERABLE_DEPENDENCY -> NotificationGroup.NEW_VULNERABLE_DEPENDENCY;
            case GROUP_PROJECT_AUDIT_CHANGE -> NotificationGroup.PROJECT_AUDIT_CHANGE;
            case GROUP_BOM_CONSUMED -> NotificationGroup.BOM_CONSUMED;
            case GROUP_BOM_PROCESSED -> NotificationGroup.BOM_PROCESSED;
            case GROUP_BOM_PROCESSING_FAILED -> NotificationGroup.BOM_PROCESSING_FAILED;
            case GROUP_VEX_CONSUMED -> NotificationGroup.VEX_CONSUMED;
            case GROUP_VEX_PROCESSED -> NotificationGroup.VEX_PROCESSED;
            case GROUP_POLICY_VIOLATION -> NotificationGroup.POLICY_VIOLATION;
            case GROUP_PROJECT_CREATED -> NotificationGroup.PROJECT_CREATED;
            case GROUP_PROJECT_VULN_ANALYSIS_COMPLETE -> NotificationGroup.PROJECT_VULN_ANALYSIS_COMPLETE;
            case GROUP_USER_CREATED -> NotificationGroup.USER_CREATED;
            case GROUP_USER_DELETED -> NotificationGroup.USER_DELETED;
            default -> throw new IllegalArgumentException("Unknown group: " + group);
        };
    }

}
