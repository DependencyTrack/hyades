package org.hyades.vulnmirror.datasource.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jeremylong.ghsa.SecurityAdvisory;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Severity;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityAffects;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GitHubToCyclonedxParserTest {

    @Test
    void shouldParseAdvisoryToBom() throws IOException {

        //given
        String jsonFile = "src/test/resources/github/github-vuln.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        SecurityAdvisory securityAdvisory = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule()).readValue(jsonString, SecurityAdvisory.class);

        Bom bom = GitHubToCyclonedxParser.parse(securityAdvisory);

        List<Vulnerability> vulnerabilities = bom.getVulnerabilitiesList();

        assertNotNull(vulnerabilities);
        assertEquals(1, bom.getVulnerabilitiesCount());
        assertEquals(3, bom.getComponentsCount());

        List<VulnerabilityAffects> vulnerabilityAffects = vulnerabilities.get(0).getAffectsList();

        assertThat(vulnerabilities).satisfiesExactly(
                vulnerability -> {
                    assertThat(vulnerability.getId()).isEqualTo("GHSA-fxwm-579q-49qq");
                    assertThat(vulnerability.getSource().getName()).isEqualTo("GITHUB");
                    assertThat(vulnerability.getDescription()).isEqualTo("In Bootstrap 4 before 4.3.1 and " +
                            "Bootstrap 3 before 3.4.1, XSS is possible in the tooltip or popover data-template attribute. " +
                            "For more information, see: https://blog.getbootstrap.com/2019/02/13/bootstrap-4-3-1-and-3-4-1/");
                    assertThat(vulnerability.getDetail()).isEqualTo("Moderate severity vulnerability that affects " +
                            "Bootstrap.Less, bootstrap, and bootstrap.sass");
                    assertThat(vulnerability.getRatingsList()).satisfiesExactly(
                            rating -> {
                                assertThat(rating.getSeverity()).isEqualTo(Severity.SEVERITY_MEDIUM);
                                assertThat(rating.getScore()).isEqualTo(0);
                            });
                });

        assertThat(vulnerabilityAffects).satisfiesExactlyInAnyOrder(
                affects -> {
                    assertThat(affects.getVersionsList()).satisfiesExactlyInAnyOrder(
                            version -> {
                                assertThat(version.getRange()).isEqualTo("vers:nuget/>= 4.0.0|< 4.3.1");
                            }
                    );
                },
                affects -> {
                    assertThat(affects.getVersionsList()).satisfiesExactlyInAnyOrder(
                            version -> {
                                assertThat(version.getRange()).isEqualTo("vers:nuget/< 4.3.1");
                            }
                    );
                },
                affects -> {
                    assertThat(affects.getVersionsList()).satisfiesExactlyInAnyOrder(
                            version -> {
                                assertThat(version.getRange()).isEqualTo("vers:nuget/>= 3.0.0|< 3.4.1");
                            }
                    );
                },
                affects -> {
                    assertThat(affects.getVersionsList()).satisfiesExactlyInAnyOrder(
                            version -> {
                                assertThat(version.getRange()).isEqualTo("vers:nuget/>= 3.0.0|< 3.4.1");
                            }
                    );
                }
        );
        assertThat(bom.getComponentsList()).satisfiesExactlyInAnyOrder(
                component -> {
                    assertThat(component.getPurl()).isEqualTo("pkg:nuget/bootstrap");
                },
                component -> {
                    assertThat(component.getPurl()).isEqualTo("pkg:nuget/bootstrap.sass");
                },
                component -> {
                    assertThat(component.getPurl()).isEqualTo("pkg:nuget/Bootstrap.Less");
                });

        assertThat(vulnerabilities.get(0).getReferencesList()).satisfiesExactlyInAnyOrder(
                reference -> {
                    assertThat(reference.getId()).isEqualTo("GHSA-fxwm-579q-49qq");
                    assertThat(reference.getSource().getName()).isEqualTo("GHSA");
                },
                reference -> {
                    assertThat(reference.getId()).isEqualTo("CVE-2019-8331");
                    assertThat(reference.getSource().getName()).isEqualTo("CVE");
                });
    }
}
