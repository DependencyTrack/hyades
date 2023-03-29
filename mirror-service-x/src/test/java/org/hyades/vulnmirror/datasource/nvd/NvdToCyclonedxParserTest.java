package org.hyades.vulnmirror.datasource.nvd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jeremylong.nvdlib.nvd.CveItem;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.ScoreMethod;
import org.cyclonedx.proto.v1_4.Severity;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityRating;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NvdToCyclonedxParserTest {

    @Test
    public void testVulnerabilityParsing() throws IOException {

        String jsonFile = "src/test/resources/nvd/nvd-vuln.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        CveItem cveItem = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule()).readValue(jsonString, CveItem.class);

        Bom result = NvdToCyclonedxParser.parse(cveItem);
        assertNotNull(result);

        assertEquals(1, result.getComponentsList().size());
        assertEquals("cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*", result.getComponentsList().get(0).getCpe());
        String bomRef = result.getComponentsList().get(0).getBomRef();

        assertEquals(2, result.getExternalReferencesList().size());

        assertEquals(1, result.getVulnerabilitiesList().size());
        Vulnerability vulnerability = result.getVulnerabilitiesList().get(0);
        assertEquals("CVE-1999-1341", vulnerability.getId());
        assertEquals("NVD", vulnerability.getSource().getName());
        assertNotNull(vulnerability.getDescription());

        assertEquals(1, vulnerability.getCwesList().size());
        assertEquals(777, vulnerability.getCwesList().get(0));

        assertEquals(1, vulnerability.getRatingsList().size());
        VulnerabilityRating rating = vulnerability.getRatingsList().get(0);
        assertEquals(ScoreMethod.SCORE_METHOD_CVSSV2, rating.getMethod());
        assertEquals("AV:L/AC:L/Au:N/C:P/I:P/A:P", rating.getVector());
        assertEquals(4.6, rating.getScore());
        assertEquals(Severity.SEVERITY_MEDIUM, rating.getSeverity());

        assertEquals(2, vulnerability.getAffectsList().size());
        assertEquals(bomRef, vulnerability.getAffectsList().get(0).getRef());
        assertEquals("vers:cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*/2.2.0|2.2.13",
                vulnerability.getAffectsList().get(0).getVersionsList().get(0).getRange());
        assertEquals(bomRef, vulnerability.getAffectsList().get(1).getRef());
        assertEquals("vers:cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*/2.3.0|2.3.18",
                vulnerability.getAffectsList().get(1).getVersionsList().get(0).getRange());
    }
}

