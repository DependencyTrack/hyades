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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class BomUploadSnykAnalysisE2ET extends AbstractE2ET {

    private String snykOrgId;
    private String snykToken;

    @Override
    @BeforeEach
    void beforeEach() throws Exception {
        snykOrgId = System.getenv("SNYK_ORG_ID");
        snykToken = System.getenv("SNYK_TOKEN");

        // Snyk does not allow unauthenticated usage; No point in running the test without credentials.
        assumeTrue(snykOrgId != null, "No Snyk organization ID provided");
        assumeTrue(snykToken != null, "No Snyk token provided");

        super.beforeEach();
    }

    @Override
    protected void customizeVulnAnalyzerContainer(GenericContainer<?> container) {
        container
                // Enable Snyk
                .withEnv("SCANNER_SNYK_ENABLED", "true")
                .withEnv("SCANNER_SNYK_API_ORG_ID", snykOrgId)
                .withEnv("SCANNER_SNYK_API_TOKENS", snykToken)
                // Disable all other scanners
                .withEnv("SCANNER_INTERNAL_ENABLED", "false")
                .withEnv("SCANNER_OSSINDEX_ENABLED", "false");
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
                            assertThat(finding.vulnerability().vulnId()).startsWith("SNYK-");
                            assertThat(finding.vulnerability().source()).isEqualTo("SNYK");
                            assertThat(finding.attribution().analyzerIdentity()).isEqualTo("SNYK_ANALYZER");
                            assertThat(finding.attribution().attributedOn()).isNotBlank();
                        }
                );
    }

}
