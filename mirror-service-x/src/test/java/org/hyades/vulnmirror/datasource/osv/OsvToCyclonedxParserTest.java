package org.hyades.vulnmirror.datasource.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.ExternalReference;
import org.cyclonedx.proto.v1_4.ScoreMethod;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityAffectedVersions;
import org.cyclonedx.proto.v1_4.VulnerabilityAffects;
import org.cyclonedx.proto.v1_4.VulnerabilityRating;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_CRITICAL;
import static org.hyades.commonutil.VulnerabilityUtil.trimSummary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                "src/test/resources/osv/osv-vulnerability-no-range.json");

        //When
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject);

        //Then
        assertNotNull(bom);
        assertNotNull(bom.getVulnerabilitiesList());
        assertEquals(1, bom.getVulnerabilitiesList().size());


        Vulnerability vulnerability = bom.getVulnerabilitiesList().get(0);
        assertNotNull(vulnerability);
        List<VulnerabilityAffects> affectedPacks= vulnerability.getAffectsList();
        assertNotNull(affectedPacks);
        assertEquals(1, affectedPacks.size());
    }

    @Test
    void testVulnerabilityRanges() throws IOException {
        //given
        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/osv/osv-vulnerability-with-ranges.json");

        //when
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject);
        List<Vulnerability> vulnerabilities = bom.getVulnerabilitiesList();

        //then
        List<Component> components = bom.getComponentsList();
        assertNotNull(components);
        assertEquals(2, components.size());

        assertNotNull(vulnerabilities);
        assertNotNull(vulnerabilities.get(0));
        List<VulnerabilityAffects> affected = vulnerabilities.get(0).getAffectsList();

        assertNotNull(affected);
        assertEquals(7, affected.size());


        VulnerabilityAffects affectedPack1 = affected.get(0);
        assertEquals(components.get(0).getBomRef(), affectedPack1.getRef());

        List<VulnerabilityAffectedVersions> versions1 = affectedPack1.getVersionsList();
        assertNotNull(versions1);
        assertEquals("vers:maven/1.0.0.RELEASE|1.0.1.RELEASE", versions1.get(0).getVersion());

        VulnerabilityAffects affectedPack3 = affected.get(3);
        assertEquals(components.get(1).getBomRef(), affectedPack3.getRef());

        List<VulnerabilityAffectedVersions> versions3 = affectedPack3.getVersionsList();
        assertNotNull(versions3);
        assertEquals(3, versions3.size());
        assertEquals("vers:maven/>=3", versions3.get(0).getRange());
        assertEquals("vers:maven/>=4|<5", versions3.get(1).getRange());
        assertEquals("vers:maven/1.0.0.RELEASE|2.0.9.RELEASE", versions3.get(2).getVersion());
    }

    @Test
    void testParseOsvToBom() throws IOException {
        //given
        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/osv/osv-GHSA-77rv-6vfw-x4gc.json");

        //when
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject);

        //then
        assertNotNull(bom);
        List<Component> components = bom.getComponentsList();
        assertNotNull(components);
        Component component = components.get(0);
        assertEquals("org.springframework.security.oauth:spring-security-oauth", component.getName());
        assertEquals("pkg:maven/org.springframework.security.oauth/spring-security-oauth", component.getPurl());

        List<ExternalReference> externalReferences = bom.getExternalReferencesList();
        assertNotNull(externalReferences);
        assertEquals(4, externalReferences.size());

        List<Vulnerability> vulnerabilities = bom.getVulnerabilitiesList();
        assertNotNull(vulnerabilities);
        assertEquals(1, vulnerabilities.size());
        Vulnerability vulnerability = vulnerabilities.get(0);
        assertEquals("GHSA-77rv-6vfw-x4gc", vulnerability.getId());

        List<VulnerabilityRating> ratings = vulnerability.getRatingsList();
        assertNotNull(ratings);
        VulnerabilityRating rating = ratings.get(0);
        assertEquals(SEVERITY_CRITICAL, rating.getSeverity());
        assertEquals(9.0, rating.getScore());
        assertEquals(ScoreMethod.SCORE_METHOD_CVSSV3, rating.getMethod());
        assertEquals("CVSS:3.1/AV:N/AC:L/PR:L/UI:R/S:C/C:H/I:H/A:H", rating.getVector());

        assertEquals(601, vulnerability.getCwesList().get(0));
        assertEquals(2, vulnerability.getAdvisoriesList().size());
        assertEquals(2, vulnerability.getCredits().getIndividualsList().size());
        assertEquals(8, vulnerability.getAffectsList().size());
        assertEquals("CVE-2019-3778", vulnerability.getReferencesList().get(0).getId());
    }

    @Test
    void testCommitHashRanges() throws IOException {

        JSONObject jsonObject = getOsvForTestingFromFile(
                "src/test/resources/osv/osv-git-commit-hash-ranges.json");
        Bom bom = new OsvToCyclonedxParser(mapper).parse(jsonObject);

        assertNotNull(bom);
        Vulnerability vulnerability = bom.getVulnerabilitiesList().get(0);
        assertEquals("OSV-2021-1820", vulnerability.getId());
        assertEquals(1, vulnerability.getAffectsList().size());
        assertTrue(vulnerability.getAffectsList().get(0).getVersionsList().get(0).getRange().isEmpty());
        assertNotNull(vulnerability.getAffectsList().get(0).getVersionsList().get(0).getVersion());
    }

    private static JSONObject getOsvForTestingFromFile(String jsonFile) throws IOException {
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        return new JSONObject(jsonString);
    }
}

