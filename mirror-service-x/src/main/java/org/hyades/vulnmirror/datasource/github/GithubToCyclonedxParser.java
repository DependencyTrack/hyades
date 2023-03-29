package org.hyades.vulnmirror.datasource.github;

import com.google.protobuf.Timestamp;
import io.github.jeremylong.ghsa.SecurityAdvisory;
import org.apache.commons.collections4.CollectionUtils;
import org.cyclonedx.proto.v1_4.Advisory;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Severity;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityRating;
import org.hyades.vulnmirror.datasource.Datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_CRITICAL;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_HIGH;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_INFO;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_LOW;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_MEDIUM;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_NONE;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_UNKNOWN;

public class GithubToCyclonedxParser {

    public static Bom parse(final SecurityAdvisory advisory) {
        final Vulnerability.Builder vuln = Vulnerability.newBuilder()
                .setSource(Source.newBuilder().setName(Datasource.GITHUB.name()).build())
                .setId(advisory.getGhsaId())
                .setDescription(advisory.getDescription())
                .addRatings(VulnerabilityRating.newBuilder()
                        //TODO check this mapping
                        .setSeverity(mapSeverity(advisory.getSeverity().value()))
                        .build());

        Optional.ofNullable(advisory.getPublishedAt())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(vuln::setPublished);

        Optional.ofNullable(advisory.getUpdatedAt())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(vuln::setUpdated);

        if (CollectionUtils.isNotEmpty(advisory.getReferences())) {
            List<Advisory> advisories = new ArrayList<>();
            advisory.getReferences().stream().filter(reference ->
                advisories.add(Advisory.newBuilder()
                        .setUrl(reference.getUrl())
                        .build())
            );
            vuln.addAllAdvisories(advisories);
        }

        Bom bom = Bom.newBuilder()
                .addVulnerabilities(vuln.build())
                .build();
        return bom;
    }

    private static Severity mapSeverity(String severity) {
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
}
