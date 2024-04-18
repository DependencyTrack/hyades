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
package org.dependencytrack.persistence.model;

import org.dependencytrack.persistence.model.ConfigProperty.Type;

public final class ConfigProperties {

    private static final String GROUP_EMAIL = "email";
    private static final String GROUP_GENERAL = "general";
    private static final String GROUP_INTERNAL = "internal";
    private static final String GROUP_INTEGRATIONS = "integrations";

    public static final ConfigProperty PROPERTY_BASE_URL = new ConfigProperty(GROUP_GENERAL, "base.url", Type.STRING);
    public static final ConfigProperty PROPERTY_CLUSTER_ID = new ConfigProperty(GROUP_INTERNAL, "cluster.id", Type.STRING);
    public static final ConfigProperty PROPERTY_SMTP_ENABLED = new ConfigProperty(GROUP_EMAIL, "smtp.enabled", Type.BOOLEAN);
    public static final ConfigProperty PROPERTY_JIRA_PASSWORD = new ConfigProperty(GROUP_INTEGRATIONS, "jira.password", Type.STRING);
    public static final ConfigProperty PROPERTY_JIRA_URL = new ConfigProperty(GROUP_INTEGRATIONS, "jira.url", Type.STRING);
    public static final ConfigProperty PROPERTY_JIRA_USERNAME = new ConfigProperty(GROUP_INTEGRATIONS, "jira.username", Type.STRING);

    private ConfigProperties() {
    }

}
