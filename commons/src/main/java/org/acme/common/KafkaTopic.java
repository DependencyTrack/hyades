package org.acme.common;

public enum KafkaTopic {


    CONFIGURATION_NOTIFICATION("notification.configuration"),
    DATASOURCE_MIRRORING_NOTIFICATION("notification.datasource_mirroring"),
    REPOSITORY_NOTIFICATION("notification.repository"),
    INTEGRATION_NOTIFICATION("notification.integration"),
    ANALYZER_NOTIFICATION("notification.analyzer"),
    BOM_CONSUMED_NOTIFICATION("notification.bom_consumed"),
    BOM_PROCESSED_NOTIFICATION("notification.bom_processed"),
    FILE_SYSTEM_NOTIFICATION("notification.file_system"),
    INDEXING_SERVICE_NOTIFICATION("notification.indexing_service"),
    NEW_VULNERABILITY_NOTIFICATION("notification.new_vulnerability"),
    NEW_VULNERABLE_DEPENDENCY_NOTIFICATION("notification.new_vulnerable_dependency"),
    POLICY_VIOLATION_NOTIFICATION("notification.policy_violation"),
    PROJECT_AUDIT_CHANGE_NOTIFICATION("notification.project_audit_change"),
    VEX_CONSUMED_NOTIFICATION("notification.vex_consumed"),
    VEX_PROCESSED_NOTIFICATION("notification.vex_processed");


    private final String name;

    KafkaTopic(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
