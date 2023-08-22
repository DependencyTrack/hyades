package org.hyades.vulnmirror.datasource.util;

import org.cyclonedx.proto.v1_4.Severity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import us.springett.cvss.Cvss;

import static org.assertj.core.api.Assertions.assertThat;

class ParserUtilTest {

    @ParameterizedTest
    @CsvSource(value = {
            "(AV:L/AC:H/Au:M/C:N/I:N/A:N), SEVERITY_UNKNOWN", // Base score 0.0
            "(AV:L/AC:H/Au:M/C:N/I:C/A:N), SEVERITY_LOW", // Base score 3.7
            "(AV:L/AC:M/Au:M/C:P/I:C/A:P), SEVERITY_MEDIUM", // Base score 5.3
            "(AV:A/AC:L/Au:M/C:C/I:C/A:C), SEVERITY_HIGH", // Base score 7.2
            "(AV:N/AC:L/Au:N/C:C/I:C/A:C), SEVERITY_HIGH", // Base score 10.0
            "CVSS:3.0/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:N, SEVERITY_UNKNOWN", // Base score 0.0
            "CVSS:3.0/AV:N/AC:H/PR:N/UI:N/S:U/C:N/I:L/A:N, SEVERITY_LOW", // Base score 3.7
            "CVSS:3.0/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:L/A:N, SEVERITY_MEDIUM", // Base score 5.3
            "CVSS:3.0/AV:N/AC:L/PR:N/UI:N/S:U/C:L/I:L/A:L, SEVERITY_HIGH", // Base score 7.3
            "CVSS:3.0/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H, SEVERITY_CRITICAL" // Base score 9.8
    })
    void testCalculateCvssSeverity(final String vector, final Severity expectedSeverity) {
        assertThat(ParserUtil.calculateCvssSeverity(Cvss.fromVector(vector))).isEqualTo(expectedSeverity);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "CRITICAL, SEVERITY_CRITICAL",
            "HIGH, SEVERITY_HIGH",
            "MEDIUM, SEVERITY_MEDIUM",
            "MODERATE, SEVERITY_MEDIUM",
            "INFO, SEVERITY_INFO",
            "NONE, SEVERITY_NONE",
            "foo, SEVERITY_UNKNOWN"
    })
    void testMapSeverity(final String inputSeverity, final Severity expectedSeverity) {
        assertThat(ParserUtil.mapSeverity(inputSeverity)).isEqualTo(expectedSeverity);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "GHSA-123, GITHUB",
            "CVE-123, NVD",
            "OSV-123, OSV",
            "GO-123, OSV",
            "foo, OSV"
    })
    void testExtractSource(final String vulnId, final String expectedSource) {
        assertThat(ParserUtil.extractSource(vulnId)).isEqualTo(expectedSource);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "Maven, maven",
            "Rust, cargo",
            "Pip, pypi",
            "RubyGems, gem",
            "Go, golang",
            "NPM, npm",
            "Composer, composer",
            "NuGet, nuget"
    })
    void testMapGitHubEcosystemToPurlType(final String ecosystem, final String purlType) {
        assertThat(ParserUtil.mapGitHubEcosystemToPurlType(ecosystem)).isEqualTo(purlType);
    }

}