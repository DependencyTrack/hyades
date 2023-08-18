package org.hyades.vulnmirror.datasource.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.util.JsonFormat;
import io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory;
import net.javacrumbs.jsonunit.core.Option;
import org.cyclonedx.proto.v1_4.Bom;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

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

        assertThatJson(JsonFormat.printer().print(bom))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "components": [{
                             "bomRef": "3c41e06b-5923-5392-a1e3-64a630c97591",
                             "purl": "pkg:nuget/bootstrap"
                           }, {
                             "bomRef": "e5dc290a-c649-5f73-b814-c9a47690a48a",
                             "purl": "pkg:nuget/bootstrap.sass"
                           }, {
                             "bomRef": "c8e5d671-0b0d-5fda-a404-730615325a7f",
                             "purl": "pkg:nuget/Bootstrap.Less"
                           }],
                           "externalReferences": [{
                             "url": "https://github.com/advisories/GHSA-fxwm-579q-49qq"
                           }],
                           "vulnerabilities": [{
                             "id": "GHSA-fxwm-579q-49qq",
                             "source": {
                               "name": "GITHUB"
                             },
                             "references": [{
                               "id": "GHSA-fxwm-579q-49qq",
                               "source": {
                                 "name": "GHSA"
                               }
                             }, {
                               "id": "CVE-2019-8331",
                               "source": {
                                 "name": "CVE"
                               }
                             }],
                             "ratings": [{
                               "method": "SCORE_METHOD_OTHER",
                               "severity": "SEVERITY_MEDIUM",
                               "source": {
                                 "name": "GITHUB"
                               }
                             }],
                             "description": "In Bootstrap 4 before 4.3.1 and Bootstrap 3 before 3.4.1, XSS is possible in the tooltip or popover data-template attribute. For more information, see: https://blog.getbootstrap.com/2019/02/13/bootstrap-4-3-1-and-3-4-1/",
                             "published": "2019-02-22T20:54:40Z",
                             "updated": "2021-12-03T14:54:43Z",
                             "affects": [{
                               "ref": "3c41e06b-5923-5392-a1e3-64a630c97591",
                               "versions": [{
                                 "range": "vers:nuget/>= 4.0.0|< 4.3.1"
                               }]
                             }, {
                               "ref": "e5dc290a-c649-5f73-b814-c9a47690a48a",
                               "versions": [{
                                 "range": "vers:nuget/< 4.3.1"
                               }]
                             }, {
                               "ref": "3c41e06b-5923-5392-a1e3-64a630c97591",
                               "versions": [{
                                 "range": "vers:nuget/>= 3.0.0|< 3.4.1"
                               }]
                             }, {
                               "ref": "c8e5d671-0b0d-5fda-a404-730615325a7f",
                               "versions": [{
                                 "range": "vers:nuget/>= 3.0.0|< 3.4.1"
                               }]
                             }],
                             "properties": [{
                               "name": "dependency-track:vuln:title",
                               "value": "Moderate severity vulnerability that affects Bootstrap.Less, bootstrap, and bootstrap.sass"
                             }]
                           }]
                         }
                        """);
    }

    @Test
    void shouldParseAdvisoryWithCweAndMultipleExternalReferences() throws IOException {

        //given
        String jsonFile = "src/test/resources/datasource/github/advisory-02.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        SecurityAdvisory securityAdvisory = MAPPER.readValue(jsonString, SecurityAdvisory.class);

        Bom bom = GitHubAdvisoryToCdxParser.parse(securityAdvisory, true);

        assertThatJson(JsonFormat.printer().print(bom))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "components": [{
                             "bomRef": "0ad65af0-d85e-58fb-ad0b-f7c0a6356d8f",
                             "purl": "pkg:npm/dojo"
                           }],
                           "externalReferences": [{
                             "url": "https://cve.mitre.org/cgi-bin/cvename.cgi?name\\u003dCVE-2015-5654"
                           }, {
                             "url": "https://snyk.io/vuln/SNYK-JS-DOJO-174933"
                           }, {
                             "url": "https://www.npmjs.com/advisories/973"
                           }, {
                             "url": "https://nvd.nist.gov/vuln/detail/CVE-2015-5654"
                           }, {
                             "url": "http://jvn.jp/en/jp/JVN13456571/index.html"
                           }, {
                             "url": "http://jvndb.jvn.jp/jvndb/JVNDB-2015-000153"
                           }, {
                             "url": "http://www-01.ibm.com/support/docview.wss?uid\\u003dswg21975256"
                           }, {
                             "url": "http://www.securityfocus.com/bid/77026"
                           }, {
                             "url": "http://www.securitytracker.com/id/1034848"
                           }, {
                             "url": "https://github.com/advisories/GHSA-p82g-2xpp-m5r3"
                           }],
                           "vulnerabilities": [{
                             "id": "GHSA-p82g-2xpp-m5r3",
                             "source": {
                               "name": "GITHUB"
                             },
                             "references": [{
                               "id": "GHSA-p82g-2xpp-m5r3",
                               "source": {
                                 "name": "GHSA"
                               }
                             }, {
                               "id": "CVE-2015-5654",
                               "source": {
                                 "name": "CVE"
                               }
                             }],
                             "ratings": [{
                               "method": "SCORE_METHOD_CVSSV3",
                               "score": 5.4,
                               "severity": "SEVERITY_MEDIUM",
                               "vector": "CVSS:3.0/AV:N/AC:L/PR:N/UI:R/S:U/C:L/I:L/A:N",
                               "source": {
                                 "name": "GITHUB"
                               }
                             }],
                             "cwes": [79],
                             "description": "Versions of `dojo` prior to 1.2.0 are vulnerable to Cross-Site Scripting (XSS). The package fails to sanitize HTML code in user-controlled input, allowing attackers to execute arbitrary JavaScript in the victim\\u0027s browser.\\n\\n\\n## Recommendation\\n\\nUpgrade to version 1.2.0 or later.",
                             "published": "2020-09-11T21:18:05Z",
                             "updated": "2023-01-06T05:01:55Z",
                             "affects": [{
                               "ref": "0ad65af0-d85e-58fb-ad0b-f7c0a6356d8f",
                               "versions": [{
                                 "range": "vers:npm/< 1.2.0"
                               }]
                             }],
                             "properties": [{
                               "name": "dependency-track:vuln:title",
                               "value": "Cross-Site Scripting in dojo"
                             }]
                           }]
                         }
                        """);
    }

    @Test
    public void testAliasSyncDisabled() throws IOException {

        //given
        String jsonFile = "src/test/resources/datasource/github/advisory-02.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        SecurityAdvisory securityAdvisory = MAPPER.readValue(jsonString, SecurityAdvisory.class);

        Bom bom = GitHubAdvisoryToCdxParser.parse(securityAdvisory, false);

        assertThatJson(JsonFormat.printer().print(bom))
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                           "components": [{
                             "bomRef": "0ad65af0-d85e-58fb-ad0b-f7c0a6356d8f",
                             "purl": "pkg:npm/dojo"
                           }],
                           "externalReferences": [{
                             "url": "https://cve.mitre.org/cgi-bin/cvename.cgi?name\\u003dCVE-2015-5654"
                           }, {
                             "url": "https://snyk.io/vuln/SNYK-JS-DOJO-174933"
                           }, {
                             "url": "https://www.npmjs.com/advisories/973"
                           }, {
                             "url": "https://nvd.nist.gov/vuln/detail/CVE-2015-5654"
                           }, {
                             "url": "http://jvn.jp/en/jp/JVN13456571/index.html"
                           }, {
                             "url": "http://jvndb.jvn.jp/jvndb/JVNDB-2015-000153"
                           }, {
                             "url": "http://www-01.ibm.com/support/docview.wss?uid\\u003dswg21975256"
                           }, {
                             "url": "http://www.securityfocus.com/bid/77026"
                           }, {
                             "url": "http://www.securitytracker.com/id/1034848"
                           }, {
                             "url": "https://github.com/advisories/GHSA-p82g-2xpp-m5r3"
                           }],
                           "vulnerabilities": [{
                             "id": "GHSA-p82g-2xpp-m5r3",
                             "source": {
                               "name": "GITHUB"
                             },
                             "ratings": [{
                               "method": "SCORE_METHOD_CVSSV3",
                               "score": 5.4,
                               "severity": "SEVERITY_MEDIUM",
                               "vector": "CVSS:3.0/AV:N/AC:L/PR:N/UI:R/S:U/C:L/I:L/A:N",
                               "source": {
                                 "name": "GITHUB"
                               }
                             }],
                             "cwes": [79],
                             "description": "Versions of `dojo` prior to 1.2.0 are vulnerable to Cross-Site Scripting (XSS). The package fails to sanitize HTML code in user-controlled input, allowing attackers to execute arbitrary JavaScript in the victim\\u0027s browser.\\n\\n\\n## Recommendation\\n\\nUpgrade to version 1.2.0 or later.",
                             "published": "2020-09-11T21:18:05Z",
                             "updated": "2023-01-06T05:01:55Z",
                             "affects": [{
                               "ref": "0ad65af0-d85e-58fb-ad0b-f7c0a6356d8f",
                               "versions": [{
                                 "range": "vers:npm/\\u003c 1.2.0"
                               }]
                             }],
                             "properties": [{
                               "name": "dependency-track:vuln:title",
                               "value": "Cross-Site Scripting in dojo"
                             }]
                           }]
                         }
                        """);
    }
}
