package org.hyades.vulnmirror.datasource.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.javacrumbs.jsonunit.core.Option;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.hyades.commonutil.VulnerabilityUtil.trimSummary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class OsvToCyclonedxParserTest {

    @Inject
    @Named("osvObjectMapper")
    ObjectMapper mapper;

    @Test
    void testTrimSummary() {

        String osvLongSummary = "In uvc_scan_chain_forward of uvc_driver.c, there is a possible linked list corruption due to an unusual root cause. This could lead to local escalation of privilege in the kernel with no additional execution privileges needed. User interaction is not needed for exploitation.";
        String trimmedSummary = trimSummary(osvLongSummary);
        assertNotNull(trimmedSummary);
        assertEquals(255, trimmedSummary.length());
        assertEquals("In uvc_scan_chain_forward of uvc_driver.c, there is a possible linked list corruption due to an unusual root cause. This could lead to local escalation of privilege in the kernel with no additional execution privileges needed. User interaction is not ne..", trimmedSummary);

        osvLongSummary = "I'm a short Summary";
        trimmedSummary = trimSummary(osvLongSummary);
        assertNotNull(trimmedSummary);
        assertEquals("I'm a short Summary", trimmedSummary);

        osvLongSummary = null;
        trimmedSummary = trimSummary(osvLongSummary);
        assertNull(trimmedSummary);
    }

    @Test
    void testVulnerabilityRangeWithNoRange() throws IOException {
        //Test file has 1 vulnerability with one affected element
        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/datasource/osv/osv-vulnerability-no-range.json");

        //When
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject, true);

        //Then
        assertThatJson(JsonFormat.printer().print(bom))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "vulnerabilities": [{
                             "id": "GSD-2022-1000008",
                             "source": {
                               "name": "OSV"
                             },
                             "ratings": [{
                               "severity": "SEVERITY_UNKNOWN"
                             }],
                             "description": "faker.js had it's version updated to 6.6.6 in NPM (which reports it as having 2,571 dependent packages that rely upon it) and the GitHub repo has been wiped of content. This appears to have been done intentionally as the repo only has a single commit (so it was likjely deleted, recreated and a single commit with \\"endgame\\" added). It appears that both GitHub and NPM have locked out the original developer accountbut that the faker.js package is still broken. Please note that this issue is directly related to GSD-2022-1000007 and appears to be part of the same incident. A fork of the repo with the original code appears to now be available at https://github.com/faker-js/faker",
                             "published": "2022-01-09T02:46:05Z",
                             "updated": "2022-01-09T11:37:01Z",
                             "properties": [{
                               "name": "dependency-track:vuln:title",
                               "value": "faker.js 6.6.6 is broken and the developer has wiped the original GitHub repo"
                             }]
                           }]
                         }
                        """);
    }

    @Test
    void testVulnerabilityRanges() throws IOException {
        //given
        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/datasource/osv/osv-vulnerability-with-ranges.json");

        //when
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject, false);

        //then
        assertThatJson(JsonFormat.printer().print(bom))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "components": [{
                             "bomRef": "5db9b99f-8362-5c69-bddb-5c9258f8d93e",
                             "name": "org.springframework.security.oauth:spring-security-oauth2",
                             "purl": "pkg:maven/org.springframework.security.oauth/spring-security-oauth2"
                           }, {
                             "bomRef": "1697132c-6230-5a5c-938d-f918e9c67279",
                             "name": "org.springframework.security.oauth:spring-security-oauth",
                             "purl": "pkg:maven/org.springframework.security.oauth/spring-security-oauth"
                           }],
                           "vulnerabilities": [{
                             "id": "GSD-2022-1000008",
                             "source": {
                               "name": "OSV"
                             },
                             "ratings": [{
                               "severity": "SEVERITY_UNKNOWN"
                             }],
                             "description": "faker.js had it's version updated to 6.6.6 in NPM (which reports it as having 2,571 dependent packages that rely upon it) and the GitHub repo has been wiped of content. This appears to have been done intentionally as the repo only has a single commit (so it was likjely deleted, recreated and a single commit with \\"endgame\\" added). It appears that both GitHub and NPM have locked out the original developer accountbut that the faker.js package is still broken. Please note that this issue is directly related to GSD-2022-1000007 and appears to be part of the same incident. A fork of the repo with the original code appears to now be available at https://github.com/faker-js/faker",
                             "published": "2022-01-09T02:46:05Z",
                             "updated": "2022-01-09T11:37:01Z",
                             "affects": [{
                               "ref": "5db9b99f-8362-5c69-bddb-5c9258f8d93e",
                               "versions": [{
                                 "version": "1.0.0.RELEASE"
                               }, {
                                 "version": "1.0.1.RELEASE"
                               }]
                             }, {
                               "ref": "1697132c-6230-5a5c-938d-f918e9c67279",
                               "versions": [{
                                 "range": "vers:maven/>=0|<2.0.17"
                               }, {
                                 "version": "1.0.0.RELEASE"
                               }, {
                                 "version": "2.0.9.RELEASE"
                               }]
                             }, {
                               "ref": "1697132c-6230-5a5c-938d-f918e9c67279",
                               "versions": [{
                                 "range": "vers:maven/>=1|<2"
                               }, {
                                 "range": "vers:maven/>=3|<4"
                               }, {
                                 "range": "vers:maven/>=0|<1"
                               }, {
                                 "version": "1.0.0.RELEASE"
                               }, {
                                 "version": "2.0.9.RELEASE"
                               }]
                             }, {
                               "ref": "1697132c-6230-5a5c-938d-f918e9c67279",
                               "versions": [{
                                 "range": "vers:maven/>=3"
                               }, {
                                 "range": "vers:maven/>=4|<5"
                               }, {
                                 "version": "1.0.0.RELEASE"
                               }, {
                                 "version": "2.0.9.RELEASE"
                               }]
                             }, {
                               "ref": "1697132c-6230-5a5c-938d-f918e9c67279",
                               "versions": [{
                                 "range": "vers:maven/>=3.1.0|<3.3.0"
                               }, {
                                 "version": "1.0.0.RELEASE"
                               }, {
                                 "version": "2.0.9.RELEASE"
                               }]
                             }, {
                               "ref": "1697132c-6230-5a5c-938d-f918e9c67279",
                               "versions": [{
                                 "range": "vers:maven/>=10|<13"
                               }, {
                                 "version": "1.0.0.RELEASE"
                               }, {
                                 "version": "2.0.9.RELEASE"
                               }]
                             }, {
                               "ref": "1697132c-6230-5a5c-938d-f918e9c67279",
                               "versions": [{
                                 "range": "vers:maven/>=10|<= 29."
                               }, {
                                 "version": "1.0.0.RELEASE"
                               }, {
                                 "version": "2.0.9.RELEASE"
                               }]
                             }],
                             "properties": [{
                               "name": "dependency-track:vuln:title",
                               "value": "faker.js 6.6.6 is broken and the developer has wiped the original GitHub repo"
                             }]
                           }]
                         }
                        """);
    }

    @Test
    void testParseOsvToBomWithAliasEnabled() throws IOException {
        //given
        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/datasource/osv/osv-GHSA-77rv-6vfw-x4gc.json");

        //when
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject, true);

        //then
        assertThatJson(JsonFormat.printer().print(bom))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                            "components": [{
                              "bomRef": "1697132c-6230-5a5c-938d-f918e9c67279",
                              "name": "org.springframework.security.oauth:spring-security-oauth",
                              "purl": "pkg:maven/org.springframework.security.oauth/spring-security-oauth"
                            }],
                            "vulnerabilities": [{
                              "id": "GHSA-77rv-6vfw-x4gc",
                              "source": {
                                "name": "GITHUB"
                              },
                              "references": [{
                                "id": "CVE-2019-3778",
                                "source": {
                                  "name": "NVD"
                                }
                              }],
                              "ratings": [{
                                "score": 9.0,
                                "severity": "SEVERITY_CRITICAL",
                                "method": "SCORE_METHOD_CVSSV3",
                                "vector": "CVSS:3.1/AV:N/AC:L/PR:L/UI:R/S:C/C:H/I:H/A:H"
                              }],
                              "cwes": [601],
                              "description": "Spring Security OAuth, versions 2.3 prior to 2.3.5, and 2.2 prior to 2.2.4, and 2.1 prior to 2.1.4, and 2.0 prior to 2.0.17, and older unsupported versions could be susceptible to an open redirector attack that can leak an authorization code.\\n\\nA malicious user or attacker can craft a request to the authorization endpoint using the authorization code grant type, and specify a manipulated redirection URI via the \\"redirect_uri\\" parameter. This can cause the authorization server to redirect the resource owner user-agent to a URI under the control of the attacker with the leaked authorization code.\\n\\nThis vulnerability exposes applications that meet all of the following requirements: Act in the role of an Authorization Server (e.g. @EnableAuthorizationServer) and uses the DefaultRedirectResolver in the AuthorizationEndpoint. \\n\\nThis vulnerability does not expose applications that: Act in the role of an Authorization Server and uses a different RedirectResolver implementation other than DefaultRedirectResolver, act in the role of a Resource Server only (e.g. @EnableResourceServer), act in the role of a Client only (e.g. @EnableOAuthClient).",
                              "published": "2019-03-14T15:39:30Z",
                              "updated": "2022-06-09T07:01:32Z",
                              "credits": {
                                "individuals": [{
                                  "name": "Skywalker"
                                }, {
                                  "name": "Solo"
                                }]
                              },
                              "affects": [{
                                "ref": "1697132c-6230-5a5c-938d-f918e9c67279",
                                "versions": [{
                                  "range": "vers:maven/>=0|<2.0.17"
                                }, {
                                  "version": "1.0.0.RELEASE"
                                }]
                              }],
                              "properties": [{
                                "name": "dependency-track:vuln:title",
                                "value": "Critical severity vulnerability that affects org.springframework.security.oauth:spring-security-oauth and org.springframework.security.oauth:spring-security-oauth2"
                              }]
                            }]
                          }
                        """);
    }

    @Test
    void testParseOsvToBomWithAliasDisabled() throws IOException {
        //given
        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/datasource/osv/osv-GHSA-77rv-6vfw-x4gc.json");

        //when
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject, false);

        //then
        assertNotNull(bom);

        List<Vulnerability> vulnerabilities = bom.getVulnerabilitiesList();
        assertNotNull(vulnerabilities);
        assertEquals(1, vulnerabilities.size());
        Vulnerability vulnerability = vulnerabilities.get(0);
        assertEquals(0, vulnerability.getReferencesList().size());
    }

    @Test
    void testCommitHashRanges() throws IOException {

        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/datasource/osv/osv-git-commit-hash-ranges.json");
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject, true);
        assertThatJson(JsonFormat.printer().print(bom))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "components": [{
                             "bomRef": "0b2df828-6b6c-554d-a0a5-70b7bfa9c7ea",
                             "name": "radare2",
                             "purl": "pkg:generic/radare2"
                           }],
                           "externalReferences": [{
                             "url": "https://bugs.chromium.org/p/oss-fuzz/issues/detail?id\\u003d48098"
                           }],
                           "vulnerabilities": [{
                             "id": "OSV-2021-1820",
                             "source": {
                               "name": "OSV"
                             },
                             "ratings": [{
                               "severity": "SEVERITY_MEDIUM"
                             }],
                             "description": "details",
                             "published": "2022-06-19T00:00:52Z",
                             "updated": "2022-06-19T00:00:52Z",
                             "affects": [{
                               "ref": "0b2df828-6b6c-554d-a0a5-70b7bfa9c7ea",
                               "versions": [{
                                 "version": "5.4.0-git"
                               }, {
                                 "version": "release-5.0.0"
                               }]
                             }],
                             "properties": [{
                               "name": "dependency-track:vuln:title",
                               "value": "Heap-buffer-overflow in r_str_utf8_codepoint"
                             }]
                           }]
                         }
                        """);
    }

    private static JSONObject getOsvForTestingFromFile(String jsonFile) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        return new JSONObject(jsonString);
    }
}

