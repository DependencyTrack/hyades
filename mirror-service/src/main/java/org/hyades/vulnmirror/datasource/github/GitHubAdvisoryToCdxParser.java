package org.hyades.vulnmirror.datasource.github;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;
import com.google.protobuf.Timestamp;
import io.github.jeremylong.openvulnerability.client.ghsa.CWEs;
import io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.ExternalReference;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityAffectedVersions;
import org.cyclonedx.proto.v1_4.VulnerabilityAffects;
import org.cyclonedx.proto.v1_4.VulnerabilityRating;
import org.cyclonedx.proto.v1_4.VulnerabilityReference;
import org.hyades.resolver.CweResolver;
import org.hyades.vulnmirror.datasource.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hyades.vulnmirror.datasource.util.ParserUtil.getBomRefIfComponentExists;
import static org.hyades.vulnmirror.datasource.util.ParserUtil.mapGitHubEcosystemToPurlType;
import static org.hyades.vulnmirror.datasource.util.ParserUtil.mapSeverity;

public class GitHubAdvisoryToCdxParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAdvisoryToCdxParser.class);

    public static Bom parse(final SecurityAdvisory advisory) {
        final Vulnerability.Builder vuln = Vulnerability.newBuilder()
                .setSource(Source.newBuilder().setName(Datasource.GITHUB.name()).build())
                .setId(advisory.getGhsaId())
                .setDescription(Optional.ofNullable(advisory.getDescription()).orElse(""))
                .setDetail(Optional.ofNullable(advisory.getSummary()).orElse(""))
                .addAllCwes(parseCwes(advisory.getCwes()));

        VulnerabilityRating.Builder rating = VulnerabilityRating.newBuilder()
                .setSeverity(mapSeverity(advisory.getSeverity().value()))
                .setScore(advisory.getCvss().getScore().doubleValue());

        Optional.ofNullable(advisory.getCvss().getVectorString()).ifPresent(rating::setVector);
        vuln.addRatings(rating.build());

        Optional.ofNullable(mapVulnerabilityReferences(advisory)).ifPresent(vuln::addAllReferences);
        Optional.ofNullable(advisory.getPublishedAt())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(vuln::setPublished);

        Optional.ofNullable(advisory.getUpdatedAt())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(vuln::setUpdated);

        Bom.Builder bom = Bom.newBuilder();
        Optional.ofNullable(mapExternalReferences(advisory)).ifPresent(bom::addAllExternalReferences);

        List<VulnerabilityAffects> affectedPackages = new ArrayList<>();

        if (advisory.getVulnerabilities() != null &&
                CollectionUtils.isNotEmpty(advisory.getVulnerabilities().getEdges())) {

            for (int i = 0; i < advisory.getVulnerabilities().getEdges().size(); i++) {
                io.github.jeremylong.openvulnerability.client.ghsa.Vulnerability gitHubVulnerability = advisory.getVulnerabilities().getEdges().get(i);
                PackageURL purl = generatePurlFromGitHubVulnerability(gitHubVulnerability);
                if (purl == null) {
                    //drop mapping if purl is null
                    break;
                }
                VulnerabilityAffects.Builder vulnerabilityAffects = VulnerabilityAffects.newBuilder();
                String bomRef = getBomRefIfComponentExists(bom.build(), purl.getCoordinates());
                if (bomRef == null) {
                    UUID uuid = UUID.randomUUID();
                    Component component = Component.newBuilder()
                            .setBomRef(uuid.toString())
                            .setPurl(purl.getCoordinates())
                            .build();

                    bom.addComponents(component);
                    bomRef = uuid.toString();
                }
                vulnerabilityAffects.setRef(bomRef);
                vulnerabilityAffects.addVersions(parseVersionRangeAffected(gitHubVulnerability));
                affectedPackages.add(vulnerabilityAffects.build());
            }
        }
        vuln.addAllAffects(affectedPackages);

        return bom.addVulnerabilities(vuln.build()).build();
    }

    private static List<VulnerabilityReference> mapVulnerabilityReferences(SecurityAdvisory advisory) {
        if (CollectionUtils.isEmpty(advisory.getIdentifiers())) {
            return null;
        }
        List<VulnerabilityReference> references = new ArrayList<>();
        advisory.getIdentifiers().forEach(identifier -> {
            VulnerabilityReference ref = VulnerabilityReference.newBuilder()
                    .setId(identifier.getValue())
                    .setSource(Source.newBuilder().setName(identifier.getType()).build())
                    .build();

            references.add(ref);
        });
        return references;
    }


    private static List<ExternalReference> mapExternalReferences(SecurityAdvisory advisory) {
        if (CollectionUtils.isEmpty(advisory.getReferences())) {
            return null;
        }
        List<ExternalReference> externalReferences = new ArrayList<>();
        advisory.getReferences().forEach(reference ->
                externalReferences.add(ExternalReference.newBuilder()
                        .setUrl(reference.getUrl())
                        .build())
        );
        return externalReferences;
    }

    private static VulnerabilityAffectedVersions parseVersionRangeAffected(final io.github.jeremylong.openvulnerability.client.ghsa.Vulnerability vuln) {

        final PackageURL purl = generatePurlFromGitHubVulnerability(vuln);
        if (purl == null) return null;
        String versionStartIncluding = null;
        String versionStartExcluding = null;
        String versionEndIncluding = null;
        String versionEndExcluding = null;
        if (vuln.getVulnerableVersionRange() != null) {
            final String[] parts = Arrays.stream(vuln.getVulnerableVersionRange().split(",")).map(String::trim).toArray(String[]::new);
            for (String part : parts) {
                if (part.startsWith(">=")) {
                    versionStartIncluding = part.trim();
                } else if (part.startsWith(">")) {
                    versionStartExcluding = part.trim();
                } else if (part.startsWith("<=")) {
                    versionEndIncluding = part.trim();
                } else if (part.startsWith("<")) {
                    versionEndExcluding = part.trim();
                } else if (part.startsWith("=")) {
                    versionStartIncluding = part.replace("=", "").trim();
                    versionEndIncluding = part.replace("=", "").trim();
                } else {
                    LOGGER.warn("Unable to determine version range of " + vuln.getPackage().getEcosystem()
                            + " : " + vuln.getPackage().getName() + " : " + vuln.getVulnerableVersionRange());
                }
            }
        }

        String uniVersionRange = "vers:" + purl.getType() + "/";
        var versionRange = VulnerabilityAffectedVersions.newBuilder();
        if (versionStartIncluding != null) {
            uniVersionRange += versionStartIncluding + "|";
        }
        if (versionStartExcluding != null) {
            uniVersionRange += versionStartExcluding + "|";
        }
        if (versionEndIncluding != null) {
            uniVersionRange += versionEndIncluding + "|";
        }
        if (versionEndExcluding != null) {
            uniVersionRange += versionEndExcluding + "|";
        }

        return versionRange.setRange(StringUtils.chop(uniVersionRange)).build();
    }

    private static List<Integer> parseCwes(CWEs weaknesses) {
        List<Integer> cwes = new ArrayList<>();
        if (weaknesses != null && CollectionUtils.isNotEmpty(weaknesses.getEdges())) {
            weaknesses.getEdges().forEach(weakness -> {
                String cweString = weakness.getCweId();
                if (cweString != null && cweString.startsWith("CWE-")) {
                    cwes.add(CweResolver.getInstance().parseCweString(cweString));
                }
            });
        }
        return cwes;
    }

    private static PackageURL generatePurlFromGitHubVulnerability(final io.github.jeremylong.openvulnerability.client.ghsa.Vulnerability vuln) {
        final String purlType = mapGitHubEcosystemToPurlType(vuln.getPackage().getEcosystem());
        try {
            if (purlType != null) {
                if (PackageURL.StandardTypes.NPM.equals(purlType) && vuln.getPackage().getName().contains("/")) {
                    final String[] parts = vuln.getPackage().getName().split("/");
                    return PackageURLBuilder.aPackageURL().withType(purlType).withNamespace(parts[0]).withName(parts[1]).build();
                } else if (PackageURL.StandardTypes.MAVEN.equals(purlType) && vuln.getPackage().getName().contains(":")) {
                    final String[] parts = vuln.getPackage().getName().split(":");
                    return PackageURLBuilder.aPackageURL().withType(purlType).withNamespace(parts[0]).withName(parts[1]).build();
                } else if (Set.of(PackageURL.StandardTypes.COMPOSER, PackageURL.StandardTypes.GOLANG).contains(purlType) && vuln.getPackage().getName().contains("/")) {
                    final String[] parts = vuln.getPackage().getName().split("/");
                    final String namespace = String.join("/", Arrays.copyOfRange(parts, 0, parts.length - 1));
                    return PackageURLBuilder.aPackageURL().withType(purlType).withNamespace(namespace).withName(parts[parts.length - 1]).build();
                } else {
                    return PackageURLBuilder.aPackageURL().withType(purlType).withName(vuln.getPackage().getName()).build();
                }
            }
        } catch (MalformedPackageURLException e) {
            LOGGER.warn("Unable to create purl from GitHub Vulnerability. Skipping " + vuln.getPackage().getEcosystem() + " : " + vuln.getPackage().getName());
        }
        return null;
    }
}
