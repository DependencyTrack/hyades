package org.acme.common;

public enum KafkaTopic {


    CONFIGURATION_NOTIFICATION("configuration-notification"),
    DATASOURCE_MIRRORING_NOTIFICATION("datasource_mirroring-notification"),
    REPOSITORY_NOTIFICATION("repository-notification"),
    INTEGRATION_NOTIFICATION("integration-notification"),
    ANALYZER_NOTIFICATION("analyzer-notification"),
    BOM_CONSUMED_NOTIFICATION("bom_consumed-notification"),
    BOM_PROCESSED_NOTIFICATION("bom_processed-notification"),
    FILE_SYSTEM_NOTIFICATION("file_system-notification"),
    INDEXING_SERVICE_NOTIFICATION("indexing_service-notification"),
    NEW_VULNERABILITY_NOTIFICATION("new_vulnerability-notification"),
    NEW_VULNERABLE_DEPENDENCY_NOTIFICATION("new_vulnerable_dependency-notification"),
    POLICY_VIOLATION_NOTIFICATION("policy_violation-notification"),
    PROJECT_AUDIT_CHANGE_NOTIFICATION("project_audit_change-notification"),
    VEX_CONSUMED_NOTIFICATION("vex_consumed-notification"),
    VEX_PROCESSED_NOTIFICATION("vex_processed-notification");


    private final String name;

    KafkaTopic(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
