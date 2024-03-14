package org.dependencytrack.common;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;

public enum KafkaTopic {

    NOTIFICATION_ANALYZER("dtrack.notification.analyzer"),
    NOTIFICATION_BOM("dtrack.notification.bom"),
    NOTIFICATION_CONFIGURATION("dtrack.notification.configuration"),
    NOTIFICATION_DATASOURCE_MIRRORING("dtrack.notification.datasource-mirroring"),
    NOTIFICATION_FILE_SYSTEM("dtrack.notification.file-system"),
    NOTIFICATION_INTEGRATION("dtrack.notification.integration"),
    NOTIFICATION_NEW_VULNERABILITY("dtrack.notification.new-vulnerability"),
    NOTIFICATION_NEW_VULNERABLE_DEPENDENCY("dtrack.notification.new-vulnerable-dependency"),
    NOTIFICATION_POLICY_VIOLATION("dtrack.notification.policy-violation"),
    NOTIFICATION_PROJECT_AUDIT_CHANGE("dtrack.notification.project-audit-change"),
    NOTIFICATION_REPOSITORY("dtrack.notification.repository"),
    NOTIFICATION_VEX("dtrack.notification.vex"),
    REPO_META_ANALYSIS_COMMAND("dtrack.repo-meta-analysis.component"),
    REPO_META_ANALYSIS_RESULT("dtrack.repo-meta-analysis.result"),
    VULN_ANALYSIS_SCANNER_RESULT("dtrack.vuln-analysis.scanner.result"),
    VULN_ANALYSIS_COMPONENT("dtrack.vuln-analysis.component"),
    VULN_ANALYSIS_RESULT("dtrack.vuln-analysis.result"),
    NEW_VULNERABILITY("dtrack.vulnerability"),
    VULNERABILITY_MIRROR_COMMAND("dtrack.vulnerability.mirror.command"),
    VULNERABILITY_MIRROR_STATE("dtrack.vulnerability.mirror.state"),
    VULNERABILITY_DIGEST("dtrack.vulnerability.digest"),
    VULNERABILITY_MIRROR_EPSS("dtrack.vulnerability.mirror.epss");

    private final String name;

    KafkaTopic(final String name) {
        this.name = name;
    }

    public String getName() {
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        var prefixConfig = config.getConfigValue("kafka.topic.prefix");
        if (prefixConfig.getValue() != null)
            return prefixConfig.getValue() + name;
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
