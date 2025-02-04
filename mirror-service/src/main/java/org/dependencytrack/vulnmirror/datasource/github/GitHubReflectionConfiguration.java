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
package org.dependencytrack.vulnmirror.datasource.github;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes of {@code open-vulnerability-clients} for the GitHub data source for reflection.
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        // Some classes of the library are package-private, so we can't reference them directly.
        classNames = {
                "io.github.jeremylong.openvulnerability.client.ghsa.AbstractPageable",
                "io.github.jeremylong.openvulnerability.client.ghsa.CVSS",
                "io.github.jeremylong.openvulnerability.client.ghsa.CVSSSeverities",
                "io.github.jeremylong.openvulnerability.client.ghsa.CWE",
                "io.github.jeremylong.openvulnerability.client.ghsa.CWEs",
                "io.github.jeremylong.openvulnerability.client.ghsa.Identifier",
                "io.github.jeremylong.openvulnerability.client.ghsa.Package",
                "io.github.jeremylong.openvulnerability.client.ghsa.PackageVersion",
                "io.github.jeremylong.openvulnerability.client.ghsa.PageInfo",
                "io.github.jeremylong.openvulnerability.client.ghsa.RateLimit",
                "io.github.jeremylong.openvulnerability.client.ghsa.Reference",
                "io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisories",
                "io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory",
                "io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisoryResponse",
                "io.github.jeremylong.openvulnerability.client.ghsa.Severity",
                "io.github.jeremylong.openvulnerability.client.ghsa.Vulnerabilities",
                "io.github.jeremylong.openvulnerability.client.ghsa.Vulnerability"
        },
        ignoreNested = false
)
class GitHubReflectionConfiguration {
}
