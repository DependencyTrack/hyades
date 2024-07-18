/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.vulnmirror.datasource.github;

import com.fasterxml.uuid.Generators;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.packageurl.PackageURLBuilder;
import com.google.protobuf.util.Timestamps;
import io.github.jeremylong.openvulnerability.client.ghsa.CWEs;
import io.github.jeremylong.openvulnerability.client.ghsa.Identifier;
import io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.proto.v1_6.Bom;
import org.cyclonedx.proto.v1_6.Component;
import org.cyclonedx.proto.v1_6.ExternalReference;
import org.cyclonedx.proto.v1_6.Property;
import org.cyclonedx.proto.v1_6.ScoreMethod;
import org.cyclonedx.proto.v1_6.Source;
import org.cyclonedx.proto.v1_6.Vulnerability;
import org.cyclonedx.proto.v1_6.VulnerabilityAffectedVersions;
import org.cyclonedx.proto.v1_6.VulnerabilityAffects;
import org.cyclonedx.proto.v1_6.VulnerabilityRating;
import org.cyclonedx.proto.v1_6.VulnerabilityReference;
import org.dependencytrack.common.cwe.CweResolver;
import org.dependencytrack.commonutil.VulnerabilityUtil;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.datasource.util.ParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.springett.cvss.Cvss;
import us.springett.cvss.CvssV2;
import us.springett.cvss.CvssV3;
import us.springett.cvss.CvssV3_1;
import us.springett.cvss.MalformedVectorException;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.github.nscuro.versatile.VersUtils.versFromGhsaRange;

public class GitHubAdvisoryToCdxParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubAdvisoryToCdxParser.class);
    private static final Source SOURCE = Source.newBuilder().setName(Datasource.GITHUB.name()).build();
    private static final String TITLE_PROPERTY_NAME = "dependency-track:vuln:title";
    private static final UUID UUID_V5_NAMESPACE = UUID.fromString("d13c94df-c6b7-4e5c-9d5b-96d77078eee8");

    public static Bom parse(final SecurityAdvisory advisory, boolean aliasSyncEnabled) {
        final Vulnerability.Builder vulnBuilder = Vulnerability.newBuilder()
                .setSource(SOURCE)
                .setId(advisory.getGhsaId())
                .setDescription(Optional.ofNullable(advisory.getDescription()).orElse(""))
                .addAllCwes(parseCwes(advisory.getCwes()));

        Optional.ofNullable(advisory.getSummary()).ifPresent(title -> vulnBuilder.addProperties(
                Property.newBuilder().setName(TITLE_PROPERTY_NAME).setValue(VulnerabilityUtil.trimSummary(title)).build()));

        parseRating(advisory).ifPresent(vulnBuilder::addRatings);

        // Alias is mapped only if aliasSync is enabled
        if (aliasSyncEnabled) {
            Optional.ofNullable(mapVulnerabilityReferences(advisory)).ifPresent(vulnBuilder::addAllReferences);
        }

        Optional.ofNullable(advisory.getPublishedAt())
                .map(ZonedDateTime::toInstant)
                .map(Instant::getEpochSecond)
                .map(Timestamps::fromSeconds)
                .ifPresent(vulnBuilder::setPublished);

        Optional.ofNullable(advisory.getUpdatedAt())
                .map(ZonedDateTime::toInstant)
                .map(Instant::getEpochSecond)
                .map(Timestamps::fromSeconds)
                .ifPresent(vulnBuilder::setUpdated);

        final var componentByPurl = new HashMap<String, Component>();
        final var vulnAffectsBuilderByBomRef = new HashMap<String, VulnerabilityAffects.Builder>();

        if (advisory.getVulnerabilities() != null &&
            CollectionUtils.isNotEmpty(advisory.getVulnerabilities().getEdges())) {

            for (final io.github.jeremylong.openvulnerability.client.ghsa.Vulnerability gitHubVulnerability : advisory.getVulnerabilities().getEdges()) {
                PackageURL purl = generatePurlFromGitHubVulnerability(gitHubVulnerability);
                if (purl == null) {
                    //drop mapping if purl is null
                    break;
                }

                final Component component = componentByPurl.computeIfAbsent(
                        purl.getCoordinates(),
                        purlCoordinates -> Component.newBuilder()
                                .setBomRef(Generators.nameBasedGenerator(UUID_V5_NAMESPACE).generate(purlCoordinates).toString())
                                .setPurl(purlCoordinates)
                                .build());

                final VulnerabilityAffects.Builder affectsBuilder = vulnAffectsBuilderByBomRef.computeIfAbsent(
                        component.getBomRef(),
                        bomRef -> VulnerabilityAffects.newBuilder()
                                .setRef(bomRef));

                var parsedVersionRange = parseVersionRangeAffected(gitHubVulnerability);
                if (parsedVersionRange != null) {
                    affectsBuilder.addVersions(parsedVersionRange);
                }
            }
        }

        // Sort components by BOM ref to ensure consistent ordering.
        final List<Component> components = componentByPurl.values().stream()
                .sorted(java.util.Comparator.comparing(Component::getBomRef))
                .toList();

        // Sort affects by BOM ref to ensure consistent ordering.
        final List<VulnerabilityAffects> vulnAffects = vulnAffectsBuilderByBomRef.values().stream()
                .map(VulnerabilityAffects.Builder::build)
                .sorted(java.util.Comparator.comparing(VulnerabilityAffects::getRef))
                .toList();

        final Bom.Builder bomBuilder = Bom.newBuilder()
                .addAllComponents(components)
                .addVulnerabilities(vulnBuilder.addAllAffects(vulnAffects));

        Optional.ofNullable(mapExternalReferences(advisory)).ifPresent(bomBuilder::addAllExternalReferences);

        return bomBuilder.build();
    }

    private static Optional<VulnerabilityRating> parseRating(final SecurityAdvisory advisory) {
        if (advisory.getCvss() != null && StringUtils.trimToNull(advisory.getCvss().getVectorString()) != null) {
            final String cvssVector = StringUtils.trimToNull(advisory.getCvss().getVectorString());
            final Cvss cvss;
            try {
                cvss = Cvss.fromVector(cvssVector);
                if (cvss == null) {
                    return Optional.empty();
                }
            } catch (MalformedVectorException e) {
                LOGGER.warn("Failed to parse rating: CVSS vector {} is malformed; Skipping", cvssVector, e);
                return Optional.empty();
            }

            final VulnerabilityRating.Builder cvssRatingBuilder = VulnerabilityRating.newBuilder()
                    .setSource(SOURCE)
                    .setVector(cvss.getVector())
                    .setScore(cvss.calculateScore().getBaseScore())
                    .setSeverity(ParserUtil.calculateCvssSeverity(cvss));
            if (cvss instanceof CvssV3_1) {
                return Optional.of(cvssRatingBuilder.setMethod(ScoreMethod.SCORE_METHOD_CVSSV31).build());
            } else if (cvss instanceof CvssV3) {
                return Optional.of(cvssRatingBuilder.setMethod(ScoreMethod.SCORE_METHOD_CVSSV3).build());
            } else if (cvss instanceof CvssV2) {
                return Optional.of(cvssRatingBuilder.setMethod(ScoreMethod.SCORE_METHOD_CVSSV2).build());
            }
        } else if (advisory.getSeverity() != null && StringUtils.trimToNull(advisory.getSeverity().value()) != null) {
            return Optional.of(VulnerabilityRating.newBuilder()
                    .setSource(SOURCE)
                    .setMethod(ScoreMethod.SCORE_METHOD_OTHER)
                    .setSeverity(ParserUtil.mapSeverity(StringUtils.trimToNull(advisory.getSeverity().value())))
                    .build());
        }

        return Optional.empty();
    }

    private static List<VulnerabilityReference> mapVulnerabilityReferences(final SecurityAdvisory advisory) {
        if (CollectionUtils.isEmpty(advisory.getIdentifiers())) {
            return null;
        }

        final var references = new ArrayList<VulnerabilityReference>();
        for (final Identifier identifier : advisory.getIdentifiers()) {
            if (advisory.getGhsaId().equals(identifier.getValue())) {
                // The advisory's ID is usually repeated in the identifiers array.
                // No need to list the vulnerability ID as reference again.
                continue;
            }

            if (!advisory.getId().equals(identifier.getValue())) {
                // TODO: Consider mapping to CNA names instead (https://github.com/DependencyTrack/hyades/issues/1297).
                final String source = switch (identifier.getType()) {
                    case "CVE" -> "NVD";
                    case "GHSA" -> "GITHUB";
                    default -> null;
                };
                if (source == null) {
                    LOGGER.warn("Unknown type {} for identifier {}; Skipping", identifier.getType(), identifier.getValue());
                    continue;
                }

                references.add(VulnerabilityReference.newBuilder()
                        .setId(identifier.getValue())
                        .setSource(Source.newBuilder().setName(source))
                        .build());
            }
        }

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
        var vulnerableVersionRange = vuln.getVulnerableVersionRange();
        try {
            var vers = versFromGhsaRange(vuln.getPackage().getEcosystem(), vulnerableVersionRange);
            var versionRange = VulnerabilityAffectedVersions.newBuilder();
            return versionRange.setRange(String.valueOf(vers)).build();
        } catch (Exception exception) {
            LOGGER.debug("Exception while parsing Github version range {}", vulnerableVersionRange, exception);
        }
        return null;
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
        final String purlType = ParserUtil.mapGitHubEcosystemToPurlType(vuln.getPackage().getEcosystem());
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
