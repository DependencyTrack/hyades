package org.hyades.e2e;

import org.hyades.apiserver.model.BomProcessingResponse;
import org.hyades.apiserver.model.BomUploadRequest;
import org.hyades.apiserver.model.BomUploadResponse;
import org.hyades.apiserver.model.Finding;
import org.hyades.apiserver.model.Project;
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
        final BomUploadResponse response = apiServerClient.uploadBom(new BomUploadRequest("foo", "bar", true, bomBase64));
        assertThat(response.token()).isNotEmpty();

        // Wait up to 15sec for the BOM processing to complete.
        await("BOM processing")
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    final BomProcessingResponse processingResponse = apiServerClient.isBomBeingProcessed(response.token());
                    assertThat(processingResponse.processing()).isFalse();
                });

        // Lookup the project we just created.
        final Project project = apiServerClient.lookupProject("foo", "bar");

        // Ensure that vulnerabilities have been reported correctly.
        final List<Finding> findings = apiServerClient.getFindings(project.uuid());
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
