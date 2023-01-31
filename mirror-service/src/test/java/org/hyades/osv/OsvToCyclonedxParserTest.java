package org.hyades.osv;

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OsvToCyclonedxParserTest {

    @Test
    public void testTrimSummary() {

        String osvLongSummary = "In uvc_scan_chain_forward of uvc_driver.c, there is a possible linked list corruption due to an unusual root cause. This could lead to local escalation of privilege in the kernel with no additional execution privileges needed. User interaction is not needed for exploitation.";
        String trimmedSummary = OsvToCyclonedxParser.trimSummary(osvLongSummary);
        assertNotNull(trimmedSummary);
        assertEquals(255, trimmedSummary.length());
        assertEquals("In uvc_scan_chain_forward of uvc_driver.c, there is a possible linked list corruption due to an unusual root cause. This could lead to local escalation of privilege in the kernel with no additional execution privileges needed. User interaction is not ne..", trimmedSummary);

        osvLongSummary = "I'm a short Summary";
        trimmedSummary = OsvToCyclonedxParser.trimSummary(osvLongSummary);
        assertNotNull(trimmedSummary);
        assertEquals("I'm a short Summary", trimmedSummary);

        osvLongSummary = null;
        trimmedSummary = OsvToCyclonedxParser.trimSummary(osvLongSummary);
        assertNull(trimmedSummary);
    }

    @Test
    public void testVulnerabilityRangeEmpty() throws IOException {

        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/osv/osv-vulnerability-no-range.json");
        Bom bom = OsvToCyclonedxParser.parse(jsonObject);
        final JSONArray affected = jsonObject.optJSONArray("affected");
        List<Vulnerability.Affect> affectedPackages = OsvToCyclonedxParser.parseAffectedRanges(affected, bom);
        assertNotNull(affectedPackages);
        assertEquals(1, affectedPackages.size());
        assertNotNull(bom.getVulnerabilities());
        assertEquals(1, bom.getVulnerabilities().size());
    }

    @Test
    public void testVulnerabilityRanges() throws IOException {
        //given
        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/osv/osv-vulnerability-with-ranges.json");
        Bom bom = OsvToCyclonedxParser.parse(jsonObject);

        //when
        JSONArray affected = jsonObject.optJSONArray("affected");
        List<Vulnerability.Affect> affectedPackages = OsvToCyclonedxParser.parseAffectedRanges(affected, bom);

        //then
        assertNotNull(affectedPackages);
        assertEquals(7, affectedPackages.size());

        List<Component> components = bom.getComponents();
        assertNotNull(components);
        assertEquals(2, components.size());

        Vulnerability.Affect affectedPackage = affectedPackages.get(0);
        assertEquals(components.get(0).getBomRef(), affectedPackage.getRef());
        List<Vulnerability.Version> versionRanges = affectedPackage.getVersions();
        assertNotNull(versionRanges);
        assertEquals("vers:maven/1.0.0.RELEASE|1.0.1.RELEASE", versionRanges.get(0).getVersion());

        affectedPackage = affectedPackages.get(3);
        assertEquals(components.get(1).getBomRef(), affectedPackage.getRef());
        versionRanges = affectedPackage.getVersions();
        assertNotNull(versionRanges);
        assertEquals("vers:maven/>=3", versionRanges.get(0).getRange());
        assertEquals("vers:maven/>=4|<5", versionRanges.get(1).getRange());
        assertEquals("vers:maven/1.0.0.RELEASE|2.0.9.RELEASE", versionRanges.get(2).getVersion());
    }

    @Test
    public void testParseOSVJson() throws IOException {

        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/osv/osv-GHSA-77rv-6vfw-x4gc.json");
        Bom bom = OsvToCyclonedxParser.parse(jsonObject);

        assertNotNull(bom);
        List<Component> components = bom.getComponents();
        assertNotNull(components);
        Component component = components.get(0);
        assertEquals("org.springframework.security.oauth:spring-security-oauth", component.getName());
        assertEquals("pkg:maven/org.springframework.security.oauth/spring-security-oauth", component.getPurl());

        List<ExternalReference> externalReferences = bom.getExternalReferences();
        assertNotNull(externalReferences);
        assertEquals(4, externalReferences.size());

        List<Vulnerability> vulnerabilities = bom.getVulnerabilities();
        assertNotNull(vulnerabilities);
        assertEquals(1, vulnerabilities.size());
        Vulnerability vulnerability = vulnerabilities.get(0);
        assertEquals("GHSA-77rv-6vfw-x4gc", vulnerability.getId());

        List<Vulnerability.Rating> ratings = vulnerability.getRatings();
        assertNotNull(ratings);
        Vulnerability.Rating rating = ratings.get(0);
        assertEquals(Vulnerability.Rating.Severity.CRITICAL, rating.getSeverity());
        assertEquals(9.0, rating.getScore());
        assertEquals(Vulnerability.Rating.Method.CVSSV3, rating.getMethod());
        assertEquals("CVSS:3.1/AV:N/AC:L/PR:L/UI:R/S:C/C:H/I:H/A:H", rating.getVector());

        assertEquals(601, vulnerability.getCwes().get(0));
        assertEquals(2, vulnerability.getAdvisories().size());
        assertEquals(2, vulnerability.getCredits().getIndividuals().size());
        assertEquals(8, vulnerability.getAffects().size());
        assertEquals("CVE-2019-3778", vulnerability.getReferences().get(0).getId());
    }

    @Test
    public void testCommitHashRanges() throws IOException {

        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/osv/osv-git-commit-hash-ranges.json");
        Bom bom = OsvToCyclonedxParser.parse(jsonObject);

        assertNotNull(bom);
        Vulnerability vulnerability = bom.getVulnerabilities().get(0);
        assertEquals("OSV-2021-1820", vulnerability.getId());
        assertEquals(1, vulnerability.getAffects().size());
        assertNull(vulnerability.getAffects().get(0).getVersions().get(0).getRange());
        assertNotNull(vulnerability.getAffects().get(0).getVersions().get(0).getVersion());
    }

    private static JSONObject getOsvForTestingFromFile(String jsonFile) throws IOException{
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        return new JSONObject(jsonString);
    }
}
