package org.hyades.vulnmirror.datasource.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory;
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
import static org.hyades.vulnmirror.datasource.github.GitHubAdvisoryToCdxParser.TITLE_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GitHubAdvisoryToCdxParserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    @Test
    void shouldParseAdvisoryToBom() throws IOException {

        //given
        String jsonFile = "src/test/resources/datasource/github/advisory.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        SecurityAdvisory securityAdvisory = MAPPER.readValue(jsonString, SecurityAdvisory.class);

        Bom bom = GitHubAdvisoryToCdxParser.parse(securityAdvisory, true);

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
                    assertThat(vulnerability.getProperties(0).getName()).isEqualTo(TITLE_PROPERTY_NAME);
                    assertThat(vulnerability.getProperties(0).getValue()).isEqualTo("Moderate severity vulnerability that affects " +
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

    @Test
    void shouldParseAdvisoryWithCweAndMultipleExternalReferences() throws IOException {

        //given
        String jsonFile = "src/test/resources/datasource/github/advisory-02.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        SecurityAdvisory securityAdvisory = MAPPER.readValue(jsonString, SecurityAdvisory.class);

        Bom bom = GitHubAdvisoryToCdxParser.parse(securityAdvisory, true);

        List<Vulnerability> vulnerabilities = bom.getVulnerabilitiesList();

        assertNotNull(vulnerabilities);
        assertEquals(1, bom.getVulnerabilitiesCount());
        assertEquals(1, bom.getComponentsCount());

        List<VulnerabilityAffects> vulnerabilityAffects = vulnerabilities.get(0).getAffectsList();

        assertThat(vulnerabilities).satisfiesExactly(
                vulnerability -> {
                    assertThat(vulnerability.getId()).isEqualTo("GHSA-p82g-2xpp-m5r3");
                    assertThat(vulnerability.getSource().getName()).isEqualTo("GITHUB");
                    assertThat(vulnerability.getDescription()).isEqualTo("Versions of `dojo` prior to 1.2.0 are vulnerable to Cross-Site Scripting (XSS). The package fails to sanitize HTML code in user-controlled input, allowing attackers to execute arbitrary JavaScript in the victim\'s browser.\n\n\n## Recommendation\n\nUpgrade to version 1.2.0 or later.");
                    assertThat(vulnerability.getProperties(0).getName()).isEqualTo(TITLE_PROPERTY_NAME);
                    assertThat(vulnerability.getProperties(0).getValue()).isEqualTo("Cross-Site Scripting in dojo");
                    assertThat(vulnerability.getRatingsList()).satisfiesExactly(
                            rating -> {
                                assertThat(rating.getSeverity()).isEqualTo(Severity.SEVERITY_MEDIUM);
                                assertThat(rating.getScore()).isEqualTo(5.4);
                                assertThat(rating.getVector()).isEqualTo("CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:L/I:L/A:N");
                            });
                    assertThat(vulnerability.getCwesList()).satisfiesExactly(
                            cwe -> {
                                assertThat(cwe.intValue()).isEqualTo(79);
                            }
                    );
                });

        assertThat(vulnerabilityAffects).satisfiesExactly(
                affects -> {
                    assertThat(affects.getVersionsList()).satisfiesExactlyInAnyOrder(
                            version -> {
                                assertThat(version.getRange()).isEqualTo("vers:npm/< 1.2.0");
                            }
                    );
                }
        );
        assertThat(bom.getComponentsList()).satisfiesExactlyInAnyOrder(
                component -> {
                    assertThat(component.getPurl()).isEqualTo("pkg:npm/dojo");
                });

        assertThat(vulnerabilities.get(0).getReferencesList()).satisfiesExactlyInAnyOrder(
                reference -> {
                    assertThat(reference.getId()).isEqualTo("GHSA-p82g-2xpp-m5r3");
                    assertThat(reference.getSource().getName()).isEqualTo("GHSA");
                },
                reference -> {
                    assertThat(reference.getId()).isEqualTo("CVE-2015-5654");
                    assertThat(reference.getSource().getName()).isEqualTo("CVE");
                });

        assertThat(bom.getExternalReferencesList()).satisfiesExactlyInAnyOrder(
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2015-5654");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("https://snyk.io/vuln/SNYK-JS-DOJO-174933");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("https://www.npmjs.com/advisories/973");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("https://nvd.nist.gov/vuln/detail/CVE-2015-5654");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("http://jvn.jp/en/jp/JVN13456571/index.html");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("http://jvndb.jvn.jp/jvndb/JVNDB-2015-000153");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("http://www-01.ibm.com/support/docview.wss?uid=swg21975256");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("http://www.securityfocus.com/bid/77026");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("http://www.securitytracker.com/id/1034848");
                },
                reference -> {
                    assertThat(reference.getUrl()).isEqualTo("https://github.com/advisories/GHSA-p82g-2xpp-m5r3");
                }
        );
    }

    @Test
    public void testAliasSyncDisabled() throws IOException {

        //given
        String jsonFile = "src/test/resources/datasource/github/advisory-02.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        SecurityAdvisory securityAdvisory = MAPPER.readValue(jsonString, SecurityAdvisory.class);

        Bom bom = GitHubAdvisoryToCdxParser.parse(securityAdvisory, false);
        List<Vulnerability> vulnerabilities = bom.getVulnerabilitiesList();

        assertNotNull(vulnerabilities);
        assertEquals(1, bom.getVulnerabilitiesCount());
        assertThat(vulnerabilities.get(0).getReferencesList()).isNullOrEmpty();
    }
}
