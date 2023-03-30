package org.hyades.vulnmirror.datasource.util;

import com.github.packageurl.PackageURL;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.Severity;

import java.util.Optional;

import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_CRITICAL;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_HIGH;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_INFO;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_LOW;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_MEDIUM;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_NONE;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_UNKNOWN;
import static org.hyades.model.Vulnerability.Source.GITHUB;
import static org.hyades.model.Vulnerability.Source.NVD;
import static org.hyades.model.Vulnerability.Source.OSV;

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

    public static Severity mapSeverity(String severity) {
        if(severity == null) {
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
            case "GHSA" -> GITHUB.name();
            case "CVE" -> NVD.name();
            default -> OSV.name();
        };
    }

    public static String mapGitHubEcosystemToPurlType(final String ecosystem) {
        switch (ecosystem.toUpperCase()) {
            case "MAVEN":
                return PackageURL.StandardTypes.MAVEN;
            case "RUST":
                return PackageURL.StandardTypes.CARGO;
            case "PIP":
                return PackageURL.StandardTypes.PYPI;
            case "RUBYGEMS":
                return PackageURL.StandardTypes.GEM;
            case "GO":
                return PackageURL.StandardTypes.GOLANG;
            case "NPM":
                return PackageURL.StandardTypes.NPM;
            case "COMPOSER":
                return PackageURL.StandardTypes.COMPOSER;
            case "NUGET":
                return PackageURL.StandardTypes.NUGET;
            default:
                return null;
        }
    }
}

