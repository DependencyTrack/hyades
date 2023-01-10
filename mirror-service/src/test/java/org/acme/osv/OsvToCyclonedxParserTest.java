package org.acme.osv;

import com.github.packageurl.MalformedPackageURLException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OsvToCyclonedxParserTest {

    OsvToCyclonedxParser parser = new OsvToCyclonedxParser();

    @Test
    public void testTrimSummary() {

        String osvLongSummary = "In uvc_scan_chain_forward of uvc_driver.c, there is a possible linked list corruption due to an unusual root cause. This could lead to local escalation of privilege in the kernel with no additional execution privileges needed. User interaction is not needed for exploitation.";
        String trimmedSummary = parser.trimSummary(osvLongSummary);
        Assertions.assertNotNull(trimmedSummary);
        Assertions.assertEquals(255, trimmedSummary.length());
        Assertions.assertEquals("In uvc_scan_chain_forward of uvc_driver.c, there is a possible linked list corruption due to an unusual root cause. This could lead to local escalation of privilege in the kernel with no additional execution privileges needed. User interaction is not ne..", trimmedSummary);

        osvLongSummary = "I'm a short Summary";
        trimmedSummary = parser.trimSummary(osvLongSummary);
        Assertions.assertNotNull(trimmedSummary);
        Assertions.assertEquals("I'm a short Summary", trimmedSummary);

        osvLongSummary = null;
        trimmedSummary = parser.trimSummary(osvLongSummary);
        Assertions.assertNull(trimmedSummary);
    }

    @Test
    public void testVulnerabilityRangeEmpty() throws IOException {

        String jsonFile = "src/test/resources/osv/osv-vulnerability-no-range.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        JSONObject jsonObject = new JSONObject(jsonString);
        final JSONArray affected = jsonObject.optJSONArray("affected");
        List<Vulnerability.Affect> affectedPackages = parser.parseAffectedRanges(affected);
        Assertions.assertNotNull(affectedPackages);
        Assertions.assertEquals(1, affectedPackages.size());
    }

    @Test
    public void testVulnerabilityRanges() throws IOException, MalformedPackageURLException {

        String jsonFile = "src/test/resources/osv/osv-vulnerability-with-ranges.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        JSONObject jsonObject = new JSONObject(jsonString);
        final JSONArray affected = jsonObject.optJSONArray("affected");
        List<Vulnerability.Affect> affectedPackages = parser.parseAffectedRanges(affected);
        Assertions.assertNotNull(affectedPackages);
        Assertions.assertEquals(7, affectedPackages.size());

        List<Component> components = parser.cyclonedxBom.getComponents();
        Assertions.assertNotNull(components);
        Assertions.assertEquals(2, components.size());

        Vulnerability.Affect affectedPackage = affectedPackages.get(0);
        Assertions.assertEquals(components.get(0).getBomRef(), affectedPackage.getRef());
        List<Vulnerability.Version> versionRanges = affectedPackage.getVersions();
        Assertions.assertNotNull(versionRanges);
        Assertions.assertEquals("vers:maven/1.0.0.RELEASE|1.0.1.RELEASE", versionRanges.get(0).getVersion());

        affectedPackage = affectedPackages.get(3);
        Assertions.assertEquals(components.get(1).getBomRef(), affectedPackage.getRef());
        versionRanges = affectedPackage.getVersions();
        Assertions.assertNotNull(versionRanges);
        Assertions.assertEquals("vers:maven/>=3", versionRanges.get(0).getRange());
        Assertions.assertEquals("vers:maven/>=4|<5", versionRanges.get(1).getRange());
        Assertions.assertEquals("vers:maven/1.0.0.RELEASE|2.0.9.RELEASE", versionRanges.get(2).getVersion());
    }

    @Test
    public void testParseOSVJson() throws IOException {

        String jsonFile = "src/test/resources/osv/osv-GHSA-77rv-6vfw-x4gc.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        JSONObject jsonObject = new JSONObject(jsonString);
        Bom bom = parser.parse(jsonObject);
        Assertions.assertNotNull(bom);
        List<Component> components = bom.getComponents();
        Assertions.assertNotNull(components);
        Component component = components.get(0);
        Assertions.assertEquals("org.springframework.security.oauth:spring-security-oauth", component.getName());
        Assertions.assertEquals("pkg:maven/org.springframework.security.oauth/spring-security-oauth", component.getPurl());

        List<ExternalReference> externalReferences = bom.getExternalReferences();
        Assertions.assertNotNull(externalReferences);
        Assertions.assertEquals(4, externalReferences.size());

        List<Vulnerability> vulnerabilities = bom.getVulnerabilities();
        Assertions.assertNotNull(vulnerabilities);
        Assertions.assertEquals(1, vulnerabilities.size());
        Vulnerability vulnerability = vulnerabilities.get(0);
        Assertions.assertEquals("GHSA-77rv-6vfw-x4gc", vulnerability.getId());

        List<Vulnerability.Rating> ratings = vulnerability.getRatings();
        Assertions.assertNotNull(ratings);
        Vulnerability.Rating rating = ratings.get(0);
        Assertions.assertEquals(Vulnerability.Rating.Severity.LOW, rating.getSeverity());
        Assertions.assertEquals(9.0, rating.getScore());
        Assertions.assertEquals(Vulnerability.Rating.Method.CVSSV3, rating.getMethod());
        Assertions.assertEquals("CVSS:3.1/AV:N/AC:L/PR:L/UI:R/S:C/C:H/I:H/A:H", rating.getVector());

        Assertions.assertEquals(601, vulnerability.getCwes().get(0));
        Assertions.assertEquals(2, vulnerability.getAdvisories().size());
        Assertions.assertEquals(2, vulnerability.getCredits().getIndividuals().size());
        Assertions.assertEquals(8, vulnerability.getAffects().size());
        Assertions.assertEquals("CVE-2019-3778", vulnerability.getReferences().get(0).getId());
    }

    @Test
    public void testCommitHashRanges() throws IOException {

        String jsonFile = "src/test/resources/osv/osv-git-commit-hash-ranges.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        JSONObject jsonObject = new JSONObject(jsonString);
        Bom bom = parser.parse(jsonObject);
        Assertions.assertNotNull(bom);
        Vulnerability vulnerability = bom.getVulnerabilities().get(0);
        Assertions.assertEquals("OSV-2021-1820", vulnerability.getId());
        Assertions.assertEquals(1, vulnerability.getAffects().size());
        Assertions.assertNull(vulnerability.getAffects().get(0).getVersions().get(0).getRange());
        Assertions.assertNotNull(vulnerability.getAffects().get(0).getVersions().get(0).getVersion());
    }
}
