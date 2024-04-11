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
package org.dependencytrack.vulnmirror.datasource.nvd;

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
                           }, {
                             "bomRef": "4ef65adb-bd4c-56bf-9917-2f493c4e127d",
                             "type": "CLASSIFICATION_OPERATING_SYSTEM",
                             "publisher": "linux",
                             "name": "linux_kernel",
                             "cpe": "cpe:2.3:o:linux:linux_kernel:-:*:*:*:*:*:*:*"
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
                             }, {
                               "ref":"4ef65adb-bd4c-56bf-9917-2f493c4e127d"
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

    @Test
    void testParseWithWildcardVersions() throws Exception {
        final byte[] cveBytes = IOUtils.resourceToByteArray("/datasource/nvd/CVE-2022-31022-all-versions-vulnerable.json");
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
                                    "type": "CLASSIFICATION_APPLICATION",
                                    "bomRef": "5f74f433-6fa5-54e0-95db-d8aae7c74e6c",
                                    "publisher": "couchbase",
                                    "name": "bleve",
                                    "cpe": "cpe:2.3:a:couchbase:bleve:*:*:*:*:*:*:*:*"
                                }
                            ],
                            "externalReferences": [
                                {
                                    "url": "https://github.com/blevesearch/bleve/commit/1c7509d6a17d36f265c90b4e8f4e3a3182fe79ff"
                                },
                                {
                                    "url": "https://github.com/blevesearch/bleve/security/advisories/GHSA-9w9f-6mg8-jp7w"
                                }
                            ],
                            "vulnerabilities": [
                                {
                                    "id": "CVE-2022-31022",
                                    "source": {
                                        "name": "NVD"
                                    },
                                    "ratings": [
                                        {
                                            "source": {
                                                "name": "NVD"
                                            },
                                            "score": 2.1,
                                            "severity": "SEVERITY_LOW",
                                            "method": "SCORE_METHOD_CVSSV2",
                                            "vector": "AV:L/AC:L/Au:N/C:N/I:P/A:N"
                                        },
                                        {
                                            "source": {
                                                "name": "NVD"
                                            },
                                            "score": 5.5,
                                            "severity": "SEVERITY_MEDIUM",
                                            "method": "SCORE_METHOD_CVSSV31",
                                            "vector": "CVSS:3.1/AV:L/AC:L/PR:L/UI:N/S:U/C:N/I:H/A:N"
                                        },
                                        {
                                            "source": {
                                                "name": "GITHUB"
                                            },
                                            "score": 6.2,
                                            "severity": "SEVERITY_MEDIUM",
                                            "method": "SCORE_METHOD_CVSSV31",
                                            "vector": "CVSS:3.1/AV:L/AC:L/PR:N/UI:N/S:U/C:N/I:H/A:N"
                                        }
                                    ],
                                    "cwes": [
                                        288,
                                        306
                                    ],
                                    "description": "Bleve is a text indexing library for go. Bleve includes HTTP utilities under bleve/http package, that are used by its sample application. These HTTP methods pave way for exploitation of a node’s filesystem where the bleve index resides, if the user has used bleve’s own HTTP (bleve/http) handlers for exposing the access to the indexes. For instance, the CreateIndexHandler (`http/index_create.go`) and DeleteIndexHandler (`http/index_delete.go`) enable an attacker to create a bleve index (directory structure) anywhere where the user running the server has the write permissions and to delete recursively any directory owned by the same user account. Users who have used the bleve/http package for exposing access to bleve index without the explicit handling for the Role Based Access Controls(RBAC) of the index assets would be impacted by this issue. There is no patch for this issue because the http package is purely intended to be used for demonstration purposes. Bleve was never designed handle the RBACs, nor it was ever advertised to be used in that way. The collaborators of this project have decided to stay away from adding any authentication or authorization to bleve project at the moment. The bleve/http package is mainly for demonstration purposes and it lacks exhaustive validation of the user inputs as well as any authentication and authorization measures. It is recommended to not use bleve/http in production use cases.",
                                    "published": "2022-06-01T20:15:08Z",
                                    "updated": "2022-06-09T14:13:24Z",
                                    "affects": [
                                        {
                                            "ref": "5f74f433-6fa5-54e0-95db-d8aae7c74e6c",
                                            "versions": [
                                                {
                                                    "range": "vers:generic/*"
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                        """);
    }

    @Test
    void testParseWithCvssV3Rating() throws Exception {
        final byte[] cveBytes = IOUtils.resourceToByteArray("/datasource/nvd/CVE-2017-5638-cvssv3-rating.json");
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
                                    "type": "CLASSIFICATION_APPLICATION",
                                    "bomRef": "3161d532-0c28-587a-9e56-9e590da32f70",
                                    "publisher": "apache",
                                    "name": "struts",
                                    "cpe": "cpe:2.3:a:apache:struts:2.3.6:*:*:*:*:*:*:*"
                                },
                                {
                                    "type": "CLASSIFICATION_APPLICATION",
                                    "bomRef": "667c89dc-403d-5dfc-bca5-8e1914e64efe",
                                    "publisher": "apache",
                                    "name": "struts",
                                    "cpe": "cpe:2.3:a:apache:struts:2.3.5:*:*:*:*:*:*:*"
                                },
                                {
                                    "type": "CLASSIFICATION_APPLICATION",
                                    "bomRef": "673da2aa-f90a-53f1-a6ff-37eee4cafd3b",
                                    "publisher": "apache",
                                    "name": "struts",
                                    "cpe": "cpe:2.3:a:apache:struts:2.5:*:*:*:*:*:*:*"
                                },
                                {
                                    "type": "CLASSIFICATION_APPLICATION",
                                    "bomRef": "b6a598c6-bdab-550a-92ea-88a9d72473a6",
                                    "publisher": "apache",
                                    "name": "struts",
                                    "cpe": "cpe:2.3:a:apache:struts:2.5.1:*:*:*:*:*:*:*"
                                }
                            ],
                            "externalReferences": [
                                {
                                    "url": "http://blog.talosintelligence.com/2017/03/apache-0-day-exploited.html"
                                }
                            ],
                            "vulnerabilities": [
                                {
                                    "id": "CVE-2017-5638",
                                    "source": {
                                        "name": "NVD"
                                    },
                                    "ratings": [
                                        {
                                            "source": {
                                                "name": "NVD"
                                            },
                                            "score": 10.0,
                                            "severity": "SEVERITY_HIGH",
                                            "method": "SCORE_METHOD_CVSSV2",
                                            "vector": "AV:N/AC:L/Au:N/C:C/I:C/A:C"
                                        },
                                        {
                                            "source": {
                                                "name": "NVD"
                                            },
                                            "score": 10.0,
                                            "severity": "SEVERITY_CRITICAL",
                                            "method": "SCORE_METHOD_CVSSV3",
                                            "vector": "CVSS:3.0/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H"
                                        }
                                    ],
                                    "cwes": [
                                        20
                                    ],
                                    "description": "The Jakarta Multipart parser in Apache Struts 2 2.3.x before 2.3.32 and 2.5.x before 2.5.10.1 has incorrect exception handling and error-message generation during file-upload attempts, which allows remote attackers to execute arbitrary commands via a crafted Content-Type, Content-Disposition, or Content-Length HTTP header, as exploited in the wild in March 2017 with a Content-Type header containing a #cmd= string.",
                                    "published": "2017-03-11T02:59:00Z",
                                    "updated": "2021-02-24T12:15:16Z",
                                    "affects": [
                                        {
                                            "ref": "3161d532-0c28-587a-9e56-9e590da32f70",
                                            "versions": [
                                                {
                                                    "version": "2.3.6"
                                                }
                                            ]
                                        },
                                        {
                                            "ref": "667c89dc-403d-5dfc-bca5-8e1914e64efe",
                                            "versions": [
                                                {
                                                    "version": "2.3.5"
                                                }
                                            ]
                                        },
                                        {
                                            "ref": "673da2aa-f90a-53f1-a6ff-37eee4cafd3b",
                                            "versions": [
                                                {
                                                    "version": "2.5"
                                                }
                                            ]
                                        },
                                        {
                                            "ref": "b6a598c6-bdab-550a-92ea-88a9d72473a6",
                                            "versions": [
                                                {
                                                    "version": "2.5.1"
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                        """);
    }

}

