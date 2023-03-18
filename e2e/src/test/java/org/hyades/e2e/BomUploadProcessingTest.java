package org.hyades.e2e;

import org.hyades.apiserver.model.BomProcessingResponse;
import org.hyades.apiserver.model.BomUploadRequest;
import org.hyades.apiserver.model.BomUploadResponse;
import org.hyades.apiserver.model.CreateVulnerabilityRequest;
import org.hyades.apiserver.model.CreateVulnerabilityRequest.AffectedComponent;
import org.hyades.apiserver.model.Finding;
import org.hyades.apiserver.model.Project;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BomUploadProcessingTest extends AbstractE2eTest {

    @Override
    protected void customizeVulnAnalyzerContainer(final GenericContainer<?> container) {
        // Disable all scanners except the internal one.
        container
                .withEnv("SCANNER_INTERNAL_ENABLED", "true")
                .withEnv("SCANNER_OSSINDEX_ENABLED", "false")
                .withEnv("SCANNER_SNYK_ENABLED", "false");
    }

    @Test
    void test() throws Exception {
        apiServerClient.createVulnerability(new CreateVulnerabilityRequest("INT-123", List.of(
                new AffectedComponent("PURL", "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2.2", "EXACT")
        )));

        final byte[] bomBytes = IOUtils.resourceToByteArray("/dtrack-apiserver-4.5.0.bom.json");
        final String bomBase64 = Base64.getEncoder().encodeToString(bomBytes);

        final BomUploadResponse response = apiServerClient.uploadBom(new BomUploadRequest("foo", "bar", true, bomBase64));
        assertThat(response.token()).isNotEmpty();

        boolean completed = false;
        for (int i = 0; i < 15; i++) {
            final BomProcessingResponse processingResponse = apiServerClient.isBomBeingProcessed(response.token());
            if (Boolean.FALSE.equals(processingResponse.processing())) {
                completed = true;
                break;
            }

            Thread.sleep(1000);
        }

        assertThat(completed).isTrue();

        final Project project = apiServerClient.lookupProject("foo", "bar");
        final List<Finding> findings = apiServerClient.getFindings(project.uuid());
        assertThat(findings).satisfiesExactly(
                finding -> {
                    assertThat(finding.component().name()).isEqualTo("jackson-databind");
                    assertThat(finding.project().name()).isEqualTo("foo");
                    assertThat(finding.vulnerability().vulnId()).isEqualTo("INT-123");
                }
        );
    }


}