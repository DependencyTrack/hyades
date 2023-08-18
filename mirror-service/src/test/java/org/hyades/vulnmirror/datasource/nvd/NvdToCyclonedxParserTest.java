package org.hyades.vulnmirror.datasource.nvd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.util.JsonFormat;
import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import net.javacrumbs.jsonunit.core.Option;
import org.cyclonedx.proto.v1_4.Bom;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class NvdToCyclonedxParserTest {

    @Test
    public void testVulnerabilityParsing() throws IOException {

        String jsonFile = "src/test/resources/datasource/nvd/cve-vuln.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        DefCveItem cveItem = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule()).readValue(jsonString, DefCveItem.class);

        Bom bov = NvdToCyclonedxParser.parse(cveItem);

        assertThatJson(JsonFormat.printer().print(bov))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "components": [{
                             "bomRef": "042fda35-385a-52e5-b7ec-be420329a9ab",
                             "type": "CLASSIFICATION_OPERATING_SYSTEM",
                             "publisher": "linux",
                             "name": "linux_kernel",
                             "cpe": "cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*"
                           }],
                           "externalReferences": [{
                             "url": "http://marc.info/?l\\u003dbugtraq\\u0026m\\u003d94061108411308\\u0026w\\u003d2"
                           }, {
                             "url": "https://exchange.xforce.ibmcloud.com/vulnerabilities/7858"
                           }],
                           "vulnerabilities": [{
                             "id": "CVE-1999-1341",
                             "source": {
                               "name": "NVD"
                             },
                             "ratings": [{
                               "score": 4.6,
                               "severity": "SEVERITY_MEDIUM",
                               "method": "SCORE_METHOD_CVSSV2",
                               "vector": "AV:L/AC:L/Au:N/C:P/I:P/A:P",
                               "source": {
                                 "name": "NVD"
                               }
                             }],
                             "cwes": [777],
                             "description": "Linux kernel before 2.3.18 or 2.2.13pre15, with SLIP and PPP options, allows local unprivileged users to forge IP packets via the TIOCSETD option on tty devices.",
                             "published": "1999-10-22T04:00:00Z",
                             "updated": "2018-09-11T14:32:55Z",
                             "affects": [{
                               "ref": "042fda35-385a-52e5-b7ec-be420329a9ab",
                               "versions": [{
                                 "range": "vers:generic/>=2.2.0|<=2.2.13"
                               }, {
                                 "range": "vers:generic/>2.3.0|<2.3.18"
                               }]
                             }]
                           }]
                         }
                        """);
    }

    @Test
    public void testVulnerabilityParsingWithNoRanges() throws IOException {

        String jsonFile = "src/test/resources/datasource/nvd/cve.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        DefCveItem cveItem = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule()).readValue(jsonString, DefCveItem.class);

        Bom bov = NvdToCyclonedxParser.parse(cveItem);
        assertThatJson(JsonFormat.printer().print(bov))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "components": [{
                             "bomRef": "02cd44fb-2f0a-569b-a508-1e179e123e38",
                             "type": "CLASSIFICATION_APPLICATION",
                             "publisher": "thinkcmf",
                             "name": "thinkcmf",
                             "cpe": "cpe:2.3:a:thinkcmf:thinkcmf:6.0.7:*:*:*:*:*:*:*"
                           }],
                           "externalReferences": [{
                             "url": "https://github.com/thinkcmf/thinkcmf/issues/736"
                           }],
                           "vulnerabilities": [{
                             "id": "CVE-2022-40489",
                             "source": {
                               "name": "NVD"
                             },
                             "ratings": [{
                               "score": 8.8,
                               "severity": "SEVERITY_HIGH",
                               "method": "SCORE_METHOD_CVSSV31",
                               "vector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:H/I:H/A:H",
                               "source": {
                                 "name": "NVD"
                               }
                             }],
                             "cwes": [352],
                             "description": "ThinkCMF version 6.0.7 is affected by a Cross Site Request Forgery (CSRF) vulnerability that allows a Super Administrator user to be injected into administrative users.",
                             "published": "2022-12-01T05:15:11Z",
                             "updated": "2022-12-02T17:17:02Z",
                             "affects": [{
                               "ref": "02cd44fb-2f0a-569b-a508-1e179e123e38",
                               "versions": [{
                                 "version": "6.0.7"
                               }]
                             }]
                           }]
                         }
                        """);
    }

    @Test
    public void testParseWithDuplicateExactVersionMatches() throws Exception {
        final byte[] cveBytes = IOUtils.resourceToByteArray("/datasource/nvd/CVE-2021-0002-duplicate-exact-version-matches.json");
        final DefCveItem cveItem = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule()).readValue(cveBytes, DefCveItem.class);

        final Bom bov = NvdToCyclonedxParser.parse(cveItem);
        assertThatJson(JsonFormat.printer().print(bov))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                          "components": [
                            {
                              "bomRef": "116876b7-5d3b-5231-9e15-3a2ed3ff817e",
                              "cpe": "cpe:2.3:o:fedoraproject:fedora:35:*:*:*:*:*:*:*",
                              "name": "fedora",
                              "publisher": "fedoraproject",
                              "type": "CLASSIFICATION_OPERATING_SYSTEM"
                            },
                            {
                              "bomRef": "bd79dec1-7de1-577f-b47f-4031d98bfefc",
                              "cpe": "cpe:2.3:o:intel:ethernet_controller_e810_firmware:*:*:*:*:*:linux:*:*",
                              "name": "ethernet_controller_e810_firmware",
                              "publisher": "intel",
                              "type": "CLASSIFICATION_OPERATING_SYSTEM"
                            },
                            {
                              "bomRef": "d8649327-fc34-5311-aff2-d3f5bafdd60c",
                              "cpe": "cpe:2.3:o:fedoraproject:fedora:33:*:*:*:*:*:*:*",
                              "name": "fedora",
                              "publisher": "fedoraproject",
                              "type": "CLASSIFICATION_OPERATING_SYSTEM"
                            },
                            {
                              "bomRef": "f1cd818e-d7f6-532d-8908-4ac64354550c",
                              "cpe": "cpe:2.3:o:fedoraproject:fedora:34:*:*:*:*:*:*:*",
                              "name": "fedora",
                              "publisher": "fedoraproject",
                              "type": "CLASSIFICATION_OPERATING_SYSTEM"
                            }
                          ],
                          "externalReferences": [
                            {
                              "url": "https://lists.fedoraproject.org/archives/list/package-announce@lists.fedoraproject.org/message/EUZYFCI7N4TFZSIGA7WGZ4Q7V3EK76GH/"
                            },
                            {
                              "url": "https://lists.fedoraproject.org/archives/list/package-announce@lists.fedoraproject.org/message/LKMUMLUH6ENNMLGTJ5AFRF6764ILEMYJ/"
                            },
                            {
                              "url": "https://lists.fedoraproject.org/archives/list/package-announce@lists.fedoraproject.org/message/MFLYHRQPDF6ZMESCI3HRNOP6D6GELPFR/"
                            },
                            {
                              "url": "https://security.netapp.com/advisory/ntap-20210827-0008/"
                            },
                            {
                              "url": "https://www.intel.com/content/www/us/en/security-center/advisory/intel-sa-00515.html"
                            }
                          ],
                          "vulnerabilities": [
                            {
                              "affects": [
                                {
                                  "ref": "116876b7-5d3b-5231-9e15-3a2ed3ff817e",
                                  "versions": [
                                    { "version": "35" }
                                  ]
                                },
                                {
                                  "ref": "bd79dec1-7de1-577f-b47f-4031d98bfefc",
                                  "versions": [
                                    { "range": "vers:generic/<1.4.11" }
                                  ]
                                },
                                {
                                  "ref": "d8649327-fc34-5311-aff2-d3f5bafdd60c",
                                  "versions": [
                                    { "version": "33" }
                                  ]
                                },
                                {
                                  "ref": "f1cd818e-d7f6-532d-8908-4ac64354550c",
                                  "versions": [
                                    { "version": "34" }
                                  ]
                                }
                              ],
                              "cwes": [ 754 ],
                              "description": "Improper conditions check in some Intel(R) Ethernet Controllers 800 series Linux drivers before version 1.4.11 may allow an authenticated user to potentially enable information disclosure or denial of service via local access.",
                              "id": "CVE-2021-0002",
                              "published": "2021-08-11T13:15:15Z",
                              "ratings": [
                                {
                                  "method": "SCORE_METHOD_CVSSV2",
                                  "score": 3.6,
                                  "severity": "SEVERITY_LOW",
                                  "source": {
                                    "name": "NVD"
                                  },
                                  "vector": "AV:L/AC:L/Au:N/C:P/I:N/A:P"
                                },
                                {
                                  "method": "SCORE_METHOD_CVSSV31",
                                  "score": 7.1,
                                  "severity": "SEVERITY_HIGH",
                                  "source": {
                                    "name": "NVD"
                                  },
                                  "vector": "CVSS:3.1/AV:L/AC:L/PR:L/UI:N/S:U/C:H/I:N/A:H"
                                }
                              ],
                              "source": {
                                "name": "NVD"
                              },
                              "updated": "2021-11-30T19:43:59Z"
                            }
                          ]
                        }
                        """);
    }

}

