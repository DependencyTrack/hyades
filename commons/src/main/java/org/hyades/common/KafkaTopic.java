package org.hyades.common;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;

public enum KafkaTopic {

    NOTIFICATION_ANALYZER("dtrack.notification.analyzer"),
    NOTIFICATION_BOM_CONSUMED("dtrack.notification.bom-consumed"),
    NOTIFICATION_BOM_PROCESSED("dtrack.notification.bom-processed"),
    NOTIFICATION_CONFIGURATION("dtrack.notification.configuration"),
    NOTIFICATION_DATASOURCE_MIRRORING("dtrack.notification.datasource-mirroring"),
    NOTIFICATION_FILE_SYSTEM("dtrack.notification.file-system"),
    NOTIFICATION_INDEXING_SERVICE("dtrack.notification.indexing-service"),
    NOTIFICATION_INTEGRATION("dtrack.notification.integration"),
    NOTIFICATION_NEW_VULNERABILITY("dtrack.notification.new-vulnerability"),
    NOTIFICATION_NEW_VULNERABLE_DEPENDENCY("dtrack.notification.new-vulnerable-dependency"),
    NOTIFICATION_POLICY_VIOLATION("dtrack.notification.policy-violation"),
    NOTIFICATION_PROJECT_AUDIT_CHANGE("dtrack.notification.project-audit-change"),
    NOTIFICATION_REPOSITORY("dtrack.notification.repository"),
    NOTIFICATION_VEX_CONSUMED("dtrack.notification.vex-consumed"),
    NOTIFICATION_VEX_PROCESSED("dtrack.notification.vex-processed"),
    REPO_META_ANALYSIS_COMMAND("dtrack.repo-meta-analysis.component"),
    REPO_META_ANALYSIS_RESULT("dtrack.repo-meta-analysis.result"),
    VULN_ANALYSIS_SCANNER_RESULT("dtrack.vuln-analysis.scanner.result"),
    VULN_ANALYSIS_COMPONENT("dtrack.vuln-analysis.component"),
    VULN_ANALYSIS_RESULT("dtrack.vuln-analysis.result"),
    MIRROR_OSV("dtrack.vulnerability.mirror.osv"),
    MIRROR_NVD("dtrack.vulnerability.mirror.nvd"),
    NEW_VULNERABILITY("dtrack.vulnerability"),
    VULNERABILITY_MIRROR_COMMAND("dtrack.vulnerability.mirror.command"),
    VULNERABILITY_MIRROR_STATE("dtrack.vulnerability.mirror.state"),
    VULNERABILITY_DIGEST("dtrack.vulnerability.digest"),
    COMPONENT_METRICS("dtrack.metrics.component"),
    PROJECT_METRICS("dtrack.metrics.project"),
    PORTFOLIO_METRICS("dtrack.metrics.portfolio");

    private final String name;

    KafkaTopic(final String name) {
        this.name = name;
    }

    public String getName() {
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        var prefixConfig = config.getConfigValue("api.topic.prefix");
        if (prefixConfig.getValue() != null)
            return prefixConfig.getValue() + name;
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
