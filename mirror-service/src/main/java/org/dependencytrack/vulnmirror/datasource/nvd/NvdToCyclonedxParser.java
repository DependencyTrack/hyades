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
package org.dependencytrack.vulnmirror.datasource.nvd;

import com.fasterxml.uuid.Generators;
import com.google.protobuf.util.Timestamps;
import io.github.jeremylong.openvulnerability.client.nvd.Config;
import io.github.jeremylong.openvulnerability.client.nvd.CpeMatch;
import io.github.jeremylong.openvulnerability.client.nvd.CveItem;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV2;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV2Data;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV3;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV3Data;
import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import io.github.jeremylong.openvulnerability.client.nvd.LangString;
import io.github.jeremylong.openvulnerability.client.nvd.Metrics;
import io.github.jeremylong.openvulnerability.client.nvd.Node;
import io.github.jeremylong.openvulnerability.client.nvd.Reference;
import io.github.jeremylong.openvulnerability.client.nvd.Weakness;
import io.github.nscuro.versatile.Comparator;
import io.github.nscuro.versatile.Vers;
import io.github.nscuro.versatile.VersException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Classification;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.ExternalReference;
import org.cyclonedx.proto.v1_4.ScoreMethod;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityAffectedVersions;
import org.cyclonedx.proto.v1_4.VulnerabilityAffects;
import org.cyclonedx.proto.v1_4.VulnerabilityRating;
import org.dependencytrack.common.cwe.CweResolver;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.springett.parsers.cpe.Cpe;
import us.springett.parsers.cpe.CpeParser;
import us.springett.parsers.cpe.exceptions.CpeParsingException;
import us.springett.parsers.cpe.values.Part;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.github.nscuro.versatile.VersUtils.versFromNvdRange;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.cyclonedx.proto.v1_4.Classification.CLASSIFICATION_APPLICATION;
import static org.cyclonedx.proto.v1_4.Classification.CLASSIFICATION_DEVICE;
import static org.cyclonedx.proto.v1_4.Classification.CLASSIFICATION_NULL;
import static org.cyclonedx.proto.v1_4.Classification.CLASSIFICATION_OPERATING_SYSTEM;
import static org.dependencytrack.vulnmirror.datasource.util.ParserUtil.mapSeverity;

/**
 * Parser and processor of NVD data feeds.
 */
public final class NvdToCyclonedxParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdToCyclonedxParser.class);
    private static final Source SOURCE_GITHUB = Source.newBuilder().setName(Datasource.GITHUB.name()).build();
    private static final Source SOURCE_NVD = Source.newBuilder().setName(Datasource.NVD.name()).build();
    private static final UUID UUID_V5_NAMESPACE = UUID.fromString("cb83f395-69ff-4b1c-83f9-c461ebd06279");

    public static Bom parse(DefCveItem nvdVuln) {
        CveItem cveItem = nvdVuln.getCve();
        final Vulnerability.Builder vulnBuilder = Vulnerability.newBuilder()
                .setSource(SOURCE_NVD)
                .setId(cveItem.getId())
                .setDescription(parseDescription(cveItem.getDescriptions()))
                .addAllRatings(parseCveImpact(cveItem.getMetrics()));

        Optional.ofNullable(cveItem.getWeaknesses())
                .map(NvdToCyclonedxParser::parseCwes)
                .ifPresent(vulnBuilder::addAllCwes);
        Optional.ofNullable(cveItem.getPublished())
                .map(ZonedDateTime::toEpochSecond)
                .map(Timestamps::fromSeconds)
                .ifPresent(vulnBuilder::setPublished);
        Optional.ofNullable(cveItem.getLastModified())
                .map(ZonedDateTime::toEpochSecond)
                .map(Timestamps::fromSeconds)
                .ifPresent(vulnBuilder::setUpdated);

        final List<CpeMatch> cpeMatches = extractCpeMatches(cveItem.getId(), cveItem.getConfigurations());
        final Pair<List<Component>, List<VulnerabilityAffects>> componentsVulnAffectsPair = processCpeMatches(cveItem.getId(), cpeMatches);
        final List<Component> components = componentsVulnAffectsPair.getLeft();
        final List<VulnerabilityAffects> vulnAffects = componentsVulnAffectsPair.getRight();

        return Bom.newBuilder()
                .addAllComponents(components)
                .addVulnerabilities(vulnBuilder.addAllAffects(vulnAffects).build())
                .addAllExternalReferences(parseReferences(cveItem.getReferences()))
                .build();
    }

    private static List<CpeMatch> extractCpeMatches(final String cveId, final List<Config> cveConfigs) {
        if (cveConfigs == null) {
            return Collections.emptyList();
        }

        return cveConfigs.stream()
                // We can't compute negation.
                .filter(config -> config.getNegate() == null || !config.getNegate())
                .map(Config::getNodes)
                .flatMap(Collection::stream)
                // We can't compute negation.
                .filter(node -> node.getNegate() == null || !node.getNegate())
                .filter(node -> node.getCpeMatch() != null)
                .map(node -> extractCpeMatchesFromNode(cveId, node))
                .flatMap(Collection::stream)
                // We currently have no interest in non-vulnerable versions.
                .filter(cpeMatch -> cpeMatch.getVulnerable() == null || cpeMatch.getVulnerable())
                .toList();
    }

    private static List<CpeMatch> extractCpeMatchesFromNode(final String cveId, final Node node) {
        // Parse all CPEs in this node, and filter out those that cannot be parsed.
        // Because multiple `CpeMatch`es can refer to the same CPE, group them by CPE.
        final Map<Cpe, List<CpeMatch>> cpeMatchesByCpe = node.getCpeMatch().stream()
                .map(cpeMatch -> {
                    try {
                        return Pair.of(CpeParser.parse(cpeMatch.getCriteria()), cpeMatch);
                    } catch (CpeParsingException e) {
                        LOGGER.warn("Failed to parse CPE of %s; Skipping".formatted(cveId), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));

        // CVE configurations may consist of applications and operating systems. In the case of
        // configurations that contain both application and operating system parts, we do not
        // want both types of CPEs to be associated to the vulnerability as it will lead to
        // false positives on the operating system. https://nvd.nist.gov/vuln/detail/CVE-2015-0312
        // is a good example of this as it contains application CPEs describing various versions
        // of Adobe Flash player, but also contains CPEs for all versions of Windows, macOS, and
        // Linux.
        //
        // Original logic ported from vanilla Dependency-Track:
        // https://github.com/DependencyTrack/dependency-track/blob/58a83978f714d5940ef7f35cc386b255cbd510f7/src/main/java/org/dependencytrack/parser/nvd/NvdParser.java#L238-L269
        if (node.getOperator() != Node.Operator.AND) {
            // Re-group `CpeMatch`es by CPE part to determine which are against applications,
            // and which against operating systems. When matches are present for both of them,
            // only use the ones for applications.
            final Map<Part, List<CpeMatch>> cpeMatchesByPart = cpeMatchesByCpe.entrySet().stream()
                    .collect(Collectors.groupingBy(
                            entry -> entry.getKey().getPart(),
                            Collectors.flatMapping(entry -> entry.getValue().stream(), Collectors.toList())));
            if (!cpeMatchesByPart.getOrDefault(Part.APPLICATION, Collections.emptyList()).isEmpty()
                    && !cpeMatchesByPart.getOrDefault(Part.OPERATING_SYSTEM, Collections.emptyList()).isEmpty()) {
                return cpeMatchesByPart.get(Part.APPLICATION);
            }
        }

        return cpeMatchesByCpe.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }

    private static Pair<List<Component>, List<VulnerabilityAffects>> processCpeMatches(final String cveId, final List<CpeMatch> cpeMatches) {
        final var componentByCpe = new HashMap<String, Component>();
        final var vulnAffectsBuilderByBomRef = new HashMap<String, VulnerabilityAffects.Builder>();

        for (final CpeMatch cpeMatch : cpeMatches) {
            final Cpe cpe;
            try {
                cpe = CpeParser.parse(cpeMatch.getCriteria());
            } catch (CpeParsingException e) {
                // Invalid CPEs were filtered out in a previous step,
                // so this should never ever fail.
                LOGGER.warn("Failed to parse CPE of {}; Skipping", cveId, e);
                continue;
            }

            final Component component = componentByCpe.computeIfAbsent(
                    cpeMatch.getCriteria(),
                    cpeStr -> Component.newBuilder()
                            .setBomRef(Generators.nameBasedGenerator(UUID_V5_NAMESPACE).generate(cpeStr).toString())
                            .setType(determineComponentType(cpe))
                            .setPublisher(cpe.getVendor())
                            .setName(cpe.getProduct())
                            .setCpe(cpeStr)
                            .build());

            final Vers versForCpeMatch;
            try {
                final Optional<Vers> optionalVers = versFromNvdRange(
                        cpeMatch.getVersionStartExcluding(),
                        cpeMatch.getVersionStartIncluding(),
                        cpeMatch.getVersionEndExcluding(),
                        cpeMatch.getVersionEndIncluding(),
                        trimToNull(cpe.getVersion())
                );
                if (optionalVers.isEmpty()) {
                    // Move to next CPE match.
                    continue;
                }

                versForCpeMatch = optionalVers.get();
            } catch (VersException exception) {
                LOGGER.warn("Failed to construct vers from CPE {}", cpe, exception);
                continue;
            }

            final VulnerabilityAffects.Builder affectsBuilder = vulnAffectsBuilderByBomRef.computeIfAbsent(
                    component.getBomRef(),
                    bomRef -> VulnerabilityAffects.newBuilder()
                            .setRef(bomRef));
            if (versForCpeMatch.constraints().size() == 1
                    && versForCpeMatch.constraints().getFirst().comparator().equals(Comparator.EQUAL)) {
                var versConstraint = versForCpeMatch.constraints().getFirst();
                // When the only constraint is an exact version match, populate the version field
                // instead of the range field. We do this despite vers supporting such cases, too,
                // e.g. via "vers:generic/1.2.3", to be more explicit.

                // CPEs with exact version matches can appear multiple times for the same CVE.
                // For example:
                //   * CVE-2014-6032 contains "cpe:2.3:a:f5:big-ip_application_security_manager:10.2.0:*:*:*:*:*:*:*" twice
                //   * CVE-2021-0002 contains "cpe:2.3:o:fedoraproject:fedora:35:*:*:*:*:*:*:*" twice
                // See:
                //   * https://services.nvd.nist.gov/rest/json/cves/2.0?cveId=CVE-2014-6032
                //   * https://services.nvd.nist.gov/rest/json/cves/2.0?cveId=CVE-2021-0002
                final boolean shouldAddVersion = affectsBuilder.getVersionsList().stream()
                        .filter(VulnerabilityAffectedVersions::hasVersion)
                        .map(VulnerabilityAffectedVersions::getVersion)
                        .noneMatch(versConstraint.version().toString()::equals);
                if (shouldAddVersion) {
                    affectsBuilder.addVersions(VulnerabilityAffectedVersions.newBuilder().setVersion(versConstraint.version().toString()));
                }
            } else {
                // Similar to how we do it for exact version matches, avoid duplicate ranges.
                final boolean shouldAddRange = affectsBuilder.getVersionsList().stream()
                        .filter(VulnerabilityAffectedVersions::hasRange)
                        .map(VulnerabilityAffectedVersions::getRange)
                        .noneMatch(versForCpeMatch.toString()::equals);
                if (shouldAddRange) {
                    affectsBuilder.addVersions(VulnerabilityAffectedVersions.newBuilder().setRange(versForCpeMatch.toString()));
                }
            }
        }

        // Sort components by BOM ref to ensure consistent ordering.
        final List<Component> components = componentByCpe.values().stream()
                .sorted(java.util.Comparator.comparing(Component::getBomRef))
                .toList();

        // Sort affects by BOM ref to ensure consistent ordering.
        final List<VulnerabilityAffects> vulnAffects = vulnAffectsBuilderByBomRef.values().stream()
                .map(VulnerabilityAffects.Builder::build)
                .sorted(java.util.Comparator.comparing(VulnerabilityAffects::getRef))
                .toList();

        return Pair.of(components, vulnAffects);
    }

    private static Classification determineComponentType(final Cpe cpe) {
        return switch (cpe.getPart()) {
            case APPLICATION -> CLASSIFICATION_APPLICATION;
            case HARDWARE_DEVICE -> CLASSIFICATION_DEVICE;
            case OPERATING_SYSTEM -> CLASSIFICATION_OPERATING_SYSTEM;
            default -> CLASSIFICATION_NULL;
        };
    }

    private static String parseDescription(List<LangString> descriptions) {
        AtomicReference<String> enDesc = new AtomicReference<>("null");

        descriptions.forEach(desc -> {
            if (desc.getLang().equalsIgnoreCase("en")) {
                enDesc.set(desc.getValue());
            }
        });
        return enDesc.get();
    }

    private static List<VulnerabilityRating> parseCveImpact(Metrics metrics) {
        List<VulnerabilityRating> ratings = new ArrayList<>();

        // CVSS V2
        List<CvssV2> baseMetricV2 = metrics.getCvssMetricV2();
        if (CollectionUtils.isNotEmpty(baseMetricV2)) {
            baseMetricV2.forEach(baseMetric -> {
                CvssV2Data cvss = baseMetric.getCvssData();
                Optional.ofNullable(cvss)
                        .map(cvss20 -> VulnerabilityRating.newBuilder()
                                .setScore(Double.parseDouble(NumberFormat.getInstance().format(cvss20.getBaseScore())))
                                .setMethod(ScoreMethod.SCORE_METHOD_CVSSV2)
                                .setVector(cvss20.getVectorString())
                                .setSeverity(mapSeverity(baseMetric.getBaseSeverity()))
                                .setSource(determineMetricSource(baseMetric.getSource()))
                                .build())
                        .ifPresent(ratings::add);
            });
        }

        // CVSS V30
        List<CvssV3> baseMetricV3 = metrics.getCvssMetricV30();
        if (CollectionUtils.isNotEmpty(baseMetricV3)) {
            baseMetricV3.forEach(baseMetric -> {
                CvssV3Data cvss = baseMetric.getCvssData();
                Optional.ofNullable(cvss)
                        .map(cvssx -> VulnerabilityRating.newBuilder()
                                .setScore(Double.parseDouble(NumberFormat.getInstance().format(cvssx.getBaseScore())))
                                .setMethod(ScoreMethod.SCORE_METHOD_CVSSV3)
                                .setVector(cvssx.getVectorString())
                                .setSeverity(mapSeverity(cvssx.getBaseSeverity().value()))
                                .setSource(determineMetricSource(baseMetric.getSource()))
                                .build())
                        .ifPresent(ratings::add);
            });
        }

        // CVSS V31
        List<CvssV3> baseMetricV31 = metrics.getCvssMetricV31();
        if (CollectionUtils.isNotEmpty(baseMetricV31)) {
            baseMetricV31.forEach(baseMetric -> {
                CvssV3Data cvss = baseMetric.getCvssData();
                Optional.ofNullable(cvss)
                        .map(cvss31 -> VulnerabilityRating.newBuilder()
                                .setScore(Double.parseDouble(NumberFormat.getInstance().format(cvss.getBaseScore())))
                                .setMethod(ScoreMethod.SCORE_METHOD_CVSSV31)
                                .setVector(cvss.getVectorString())
                                .setSeverity(mapSeverity(cvss.getBaseSeverity().value()))
                                .setSource(determineMetricSource(baseMetric.getSource()))
                                .build())
                        .ifPresent(ratings::add);
            });
        }
        return ratings;
    }

    private static List<Integer> parseCwes(List<Weakness> weaknesses) {
        List<Integer> cwes = new ArrayList<>();
        weaknesses.forEach(weakness -> {
            List<LangString> descList = weakness.getDescription();
            descList.forEach(desc -> {
                if (desc.getLang().equalsIgnoreCase("en")) {
                    String cweString = desc.getValue();
                    if (cweString != null && cweString.startsWith("CWE-")) {
                        cwes.add(CweResolver.getInstance().parseCweString(cweString));
                    }
                }
            });
        });
        return cwes;
    }

    private static List<ExternalReference> parseReferences(List<Reference> references) {
        List<ExternalReference> externalReferences = new ArrayList<>();
        references.forEach(reference -> externalReferences.add(ExternalReference.newBuilder()
                .setUrl(reference.getUrl())
                .build()));
        return externalReferences;
    }

    private static Source determineMetricSource(final String source) {
        // If the vulnerability was originally reported to GitHub as a GHSA,
        // and GitHub registered an accompanying CVE, the GitHub rating will
        // be listed by the NVD as well.
        if ("nvd@nist.gov".equals(source)) {
            return SOURCE_NVD;
        } else if ("security-advisories@github.com".equals(source)) {
            return SOURCE_GITHUB;
        }

        // Fall back to NVD if we don't recognize the source.
        // The if statement above may be refined over time as we encounter more
        // sources reporting ratings. There's no documentation on this, so at
        // the moment we only know about GitHub as alternative source.
        return SOURCE_NVD;
    }

}
