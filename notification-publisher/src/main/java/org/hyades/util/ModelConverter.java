package org.hyades.util;

import org.hyades.notification.model.NotificationGroup;
import org.hyades.notification.model.NotificationLevel;
import org.hyades.proto.notification.v1.Group;
import org.hyades.proto.notification.v1.Level;

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
            case GROUP_INDEXING_SERVICE -> NotificationGroup.INDEXING_SERVICE;
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
            default -> throw new IllegalArgumentException("Unknown group: " + group);
        };
    }

}
