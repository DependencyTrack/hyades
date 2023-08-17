package org.hyades.vulnmirror.datasource.nvd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.util.JsonFormat;
import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import net.javacrumbs.jsonunit.core.Option;
import org.cyclonedx.proto.v1_4.Bom;
import org.junit.jupiter.api.Test;

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
                               "vector": "AV:L/AC:L/Au:N/C:P/I:P/A:P"
                             }],
                             "cwes": [777],
                             "description": "Linux kernel before 2.3.18 or 2.2.13pre15, with SLIP and PPP options, allows local unprivileged users to forge IP packets via the TIOCSETD option on tty devices.",
                             "published": "1999-10-22T04:00:00Z",
                             "updated": "2018-09-11T14:32:55Z",
                             "affects": [{
                               "ref": "042fda35-385a-52e5-b7ec-be420329a9ab",
                               "versions": [{
                                 "range": "vers:generic/2.2.0|2.2.13"
                               }]
                             }, {
                               "ref": "042fda35-385a-52e5-b7ec-be420329a9ab",
                               "versions": [{
                                 "range": "vers:generic/2.3.0|2.3.18"
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
                               "vector": "CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:H/I:H/A:H"
                             }],
                             "cwes": [352],
                             "description": "ThinkCMF version 6.0.7 is affected by a Cross Site Request Forgery (CSRF) vulnerability that allows a Super Administrator user to be injected into administrative users.",
                             "published": "2022-12-01T05:15:11Z",
                             "updated": "2022-12-02T17:17:02Z",
                             "affects": [{
                               "ref": "02cd44fb-2f0a-569b-a508-1e179e123e38"
                             }]
                           }]
                         }
                        """);
    }
}

