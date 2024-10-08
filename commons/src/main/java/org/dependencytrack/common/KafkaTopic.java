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
    NOTIFICATION_USER("dtrack.notification.user"),
    REPO_META_ANALYSIS_COMMAND("dtrack.repo-meta-analysis.component"),
    REPO_META_ANALYSIS_RESULT("dtrack.repo-meta-analysis.result"),
    VULN_ANALYSIS_SCANNER_RESULT("dtrack.vuln-analysis.scanner.result"),
    VULN_ANALYSIS_COMPONENT("dtrack.vuln-analysis.component"),
    VULN_ANALYSIS_RESULT("dtrack.vuln-analysis.result"),
    NEW_VULNERABILITY("dtrack.vulnerability"),
    VULNERABILITY_MIRROR_COMMAND("dtrack.vulnerability.mirror.command"),
    VULNERABILITY_MIRROR_STATE("dtrack.vulnerability.mirror.state"),
    VULNERABILITY_DIGEST("dtrack.vulnerability.digest"),
    NEW_EPSS("dtrack.epss");

    private final String name;

    KafkaTopic(final String name) {
        this.name = name;
    }

    public String getName() {
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        var prefixConfig = config.getConfigValue("dt.kafka.topic.prefix");
        if (prefixConfig.getValue() != null)
            return prefixConfig.getValue() + name;
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
