package org.dependencytrack.vulnmirror.datasource.util;

import com.github.packageurl.PackageURL;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.Severity;
import us.springett.cvss.Cvss;
import us.springett.cvss.CvssV2;

import java.util.Optional;

import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_CRITICAL;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_HIGH;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_INFO;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_LOW;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_MEDIUM;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_NONE;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_UNKNOWN;

public class ParserUtil {

    public static String getBomRefIfComponentExists(Bom cyclonedxBom, String purl) {
        if (cyclonedxBom.getComponentsList() != null && purl != null) {
            Optional<Component> existingComponent = cyclonedxBom.getComponentsList().stream().filter(c ->
                    c.getPurl().equalsIgnoreCase(purl)).findFirst();
            if (existingComponent.isPresent()) {
                return existingComponent.get().getBomRef();
            }
        }
        return null;
    }

    public static Severity calculateCvssSeverity(final Cvss cvss) {
        if (cvss == null) {
            return SEVERITY_UNKNOWN;
        }

        final double baseScore = cvss.calculateScore().getBaseScore();
        if (cvss instanceof us.springett.cvss.CvssV3 || cvss instanceof io.github.jeremylong.openvulnerability.client.nvd.CvssV3) {
            if (baseScore >= 9) {
                return SEVERITY_CRITICAL;
            } else if (baseScore >= 7) {
                return SEVERITY_HIGH;
            } else if (baseScore >= 4) {
                return SEVERITY_MEDIUM;
            } else if (baseScore > 0) {
                return SEVERITY_LOW;
            }
        } else if (cvss instanceof CvssV2) {
            if (baseScore >= 7) {
                return SEVERITY_HIGH;
            } else if (baseScore >= 4) {
                return SEVERITY_MEDIUM;
            } else if (baseScore > 0) {
                return SEVERITY_LOW;
            }
        }

        return SEVERITY_UNKNOWN;
    }

    public static Severity mapSeverity(String severity) {
        if (severity == null) {
            return SEVERITY_UNKNOWN;
        }
        return switch (severity) {
            case "CRITICAL" -> SEVERITY_CRITICAL;
            case "HIGH" -> SEVERITY_HIGH;
            case "MEDIUM", "MODERATE" -> SEVERITY_MEDIUM;
            case "LOW" -> SEVERITY_LOW;
            case "INFO" -> SEVERITY_INFO;
            case "NONE" -> SEVERITY_NONE;
            default -> SEVERITY_UNKNOWN;
        };
    }

    public static String extractSource(String vulnId) {
        String sourceId = vulnId.split("-")[0];
        return switch (sourceId) {
            case "GHSA" -> "GITHUB";
            case "CVE" -> "NVD";
            default -> "OSV";
        };
    }

    public static String mapGitHubEcosystemToPurlType(final String ecosystem) {
        return switch (ecosystem.toUpperCase()) {
            case "MAVEN" -> PackageURL.StandardTypes.MAVEN;
            case "RUST" -> PackageURL.StandardTypes.CARGO;
            case "PIP" -> PackageURL.StandardTypes.PYPI;
            case "RUBYGEMS" -> PackageURL.StandardTypes.GEM;
            case "GO" -> PackageURL.StandardTypes.GOLANG;
            case "NPM" -> PackageURL.StandardTypes.NPM;
            case "COMPOSER" -> PackageURL.StandardTypes.COMPOSER;
            case "NUGET" -> PackageURL.StandardTypes.NUGET;
            default -> null;
        };
    }
}
