package org.acme.nvd;

import io.github.jeremylong.nvdlib.nvd.CveItem;
import org.acme.client.MirrorClientConfig;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NvdToCyclonedxParserTest {

    NvdToCyclonedxParser parser = new NvdToCyclonedxParser();
    MirrorClientConfig mirrorClientConfig = new MirrorClientConfig();

    @Test
    public void testVulnerabilityRangeEmpty() throws IOException {

        String jsonFile = "src/test/resources/nvd/nvd-vuln.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        CveItem cveItem = mirrorClientConfig.objectMapper().readValue(jsonString, CveItem.class);
        Bom result = parser.parse(cveItem);
        Assertions.assertNotNull(result);

        Assertions.assertEquals(1, result.getComponents().size());
        Assertions.assertEquals("cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*", result.getComponents().get(0).getCpe());
        String bomRef = result.getComponents().get(0).getBomRef();

        Assertions.assertEquals(2, result.getExternalReferences().size());

        Assertions.assertEquals(1, result.getVulnerabilities().size());
        Vulnerability vulnerability = result.getVulnerabilities().get(0);
        Assertions.assertEquals("CVE-1999-1341", vulnerability.getId());
        Assertions.assertEquals("NVD", vulnerability.getSource().getName());
        Assertions.assertNotNull(vulnerability.getDescription());

        Assertions.assertEquals(1, vulnerability.getCwes().size());
        Assertions.assertEquals(777, vulnerability.getCwes().get(0));

        Assertions.assertEquals(1, vulnerability.getRatings().size());
        Vulnerability.Rating rating = vulnerability.getRatings().get(0);
        Assertions.assertEquals(Vulnerability.Rating.Method.CVSSV2, rating.getMethod());
        Assertions.assertEquals("AV:L/AC:L/Au:N/C:P/I:P/A:P", rating.getVector());
        Assertions.assertEquals(4.6, rating.getScore());
        Assertions.assertEquals(Vulnerability.Rating.Severity.MEDIUM, rating.getSeverity());

        Assertions.assertEquals(2, vulnerability.getAffects().size());
        Assertions.assertEquals(bomRef, vulnerability.getAffects().get(0).getRef());
        Assertions.assertEquals("vers:cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*/2.2.0|2.2.13",
                vulnerability.getAffects().get(0).getVersions().get(0).getRange());
        Assertions.assertEquals(bomRef, vulnerability.getAffects().get(1).getRef());
        Assertions.assertEquals("vers:cpe:2.3:o:linux:linux_kernel:*:*:*:*:*:*:*:*/2.3.0|2.3.18",
                vulnerability.getAffects().get(1).getVersions().get(0).getRange());
    }
}
