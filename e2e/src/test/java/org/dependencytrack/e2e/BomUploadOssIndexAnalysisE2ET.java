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
package org.dependencytrack.e2e;

import org.dependencytrack.apiserver.model.EventProcessingResponse;
import org.dependencytrack.apiserver.model.BomUploadRequest;
import org.dependencytrack.apiserver.model.Finding;
import org.dependencytrack.apiserver.model.Project;
import org.dependencytrack.apiserver.model.WorkflowTokenResponse;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class BomUploadOssIndexAnalysisE2ET extends AbstractE2ET {

    @Override
    protected void customizeVulnAnalyzerContainer(GenericContainer<?> container) {
        // Username and token are optional, OSS Index can be used without authentication.
        final String ossIndexUsername = Optional.ofNullable(System.getenv("OSSINDEX_USERNAME")).orElse("");
        final String ossIndexToken = Optional.ofNullable(System.getenv("OSSINDEX_TOKEN")).orElse("");

        container
                // Enable OSS Index
                .withEnv("SCANNER_OSSINDEX_ENABLED", "true")
                .withEnv("SCANNER_OSSINDEX_API_USERNAME", ossIndexUsername)
                .withEnv("SCANNER_OSSINDEX_API_TOKEN", ossIndexToken)
                // Disable all other scanners
                .withEnv("SCANNER_INTERNAL_ENABLED", "false")
                .withEnv("SCANNER_SNYK_ENABLED", "false");
    }

    @Test
    void test() throws Exception {
        // Parse and base64 encode a BOM.
        final byte[] bomBytes = IOUtils.resourceToByteArray("/dtrack-apiserver-4.5.0.bom.json");
        final String bomBase64 = Base64.getEncoder().encodeToString(bomBytes);

        // Upload the BOM
        final WorkflowTokenResponse response = apiServerClient.uploadBom(new BomUploadRequest("foo", "bar", true, bomBase64));
        assertThat(response.token()).isNotEmpty();

        // Wait up to 15sec for the BOM processing to complete.
        await("BOM processing")
                .atMost(Duration.ofSeconds(30))
                .pollDelay(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    final EventProcessingResponse processingResponse = apiServerClient.isEventBeingProcessed(response.token());
                    assertThat(processingResponse.processing()).isFalse();
                });

        // Lookup the project we just created.
        final Project project = apiServerClient.lookupProject("foo", "bar");

        // Ensure that vulnerabilities have been reported correctly.
        final List<Finding> findings = apiServerClient.getFindings(project.uuid(), false);
        assertThat(findings)
                .hasSizeGreaterThan(1)
                .allSatisfy(
                        finding -> {
                            assertThat(finding.vulnerability()).satisfiesAnyOf(
                                    vuln -> {
                                        assertThat(vuln.vulnId()).startsWith("CVE-");
                                        assertThat(vuln.source()).isEqualTo("NVD");
                                    },
                                    vuln -> {
                                        assertThat(vuln.vulnId()).startsWith("sonatype-");
                                        assertThat(vuln.source()).isEqualTo("OSSINDEX");
                                    }
                            );
                            assertThat(finding.attribution().analyzerIdentity()).isEqualTo("OSSINDEX_ANALYZER");
                            assertThat(finding.attribution().attributedOn()).isNotBlank();
                        }
                );
    }

}
