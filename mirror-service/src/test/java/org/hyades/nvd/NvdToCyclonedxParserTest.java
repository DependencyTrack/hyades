package org.hyades.nvd;

import io.github.jeremylong.nvdlib.nvd.CveItem;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NvdToCyclonedxParserTest {

    NvdToCyclonedxParser parser = new NvdToCyclonedxParser();
    NvdProcessorConfig nvdProcessorConfig = new NvdProcessorConfig();

    @Test
    public void testVulnerabilityParsing() throws IOException {

        String jsonFile = "src/test/resources/nvd/nvd-vuln.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        CveItem cveItem = nvdProcessorConfig.objectMapper().readValue(jsonString, CveItem.class);
        Bom result = parser.parse(cveItem);
        assertNotNull(result);

        assertEquals(1, result.getComponents().size());
        assertEquals("cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*", result.getComponents().get(0).getCpe());
        String bomRef = result.getComponents().get(0).getBomRef();

        assertEquals(2, result.getExternalReferences().size());

        assertEquals(1, result.getVulnerabilities().size());
        Vulnerability vulnerability = result.getVulnerabilities().get(0);
        assertEquals("CVE-1999-1341", vulnerability.getId());
        assertEquals("NVD", vulnerability.getSource().getName());
        assertNotNull(vulnerability.getDescription());

        assertEquals(1, vulnerability.getCwes().size());
        assertEquals(777, vulnerability.getCwes().get(0));

        assertEquals(1, vulnerability.getRatings().size());
        Vulnerability.Rating rating = vulnerability.getRatings().get(0);
        assertEquals(Vulnerability.Rating.Method.CVSSV2, rating.getMethod());
        assertEquals("AV:L/AC:L/Au:N/C:P/I:P/A:P", rating.getVector());
        assertEquals(4.6, rating.getScore());
        assertEquals(Vulnerability.Rating.Severity.MEDIUM, rating.getSeverity());

        assertEquals(2, vulnerability.getAffects().size());
        assertEquals(bomRef, vulnerability.getAffects().get(0).getRef());
        assertEquals("vers:cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*/2.2.0|2.2.13",
                vulnerability.getAffects().get(0).getVersions().get(0).getRange());
        assertEquals(bomRef, vulnerability.getAffects().get(1).getRef());
        assertEquals("vers:cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*/2.3.0|2.3.18",
                vulnerability.getAffects().get(1).getVersions().get(0).getRange());
    }
}
