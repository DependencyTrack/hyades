package org.hyades.vulnmirror.datasource.nvd;

import com.google.protobuf.Timestamp;
import io.github.jeremylong.openvulnerability.client.nvd.Config;
import io.github.jeremylong.openvulnerability.client.nvd.CpeMatch;
import io.github.jeremylong.openvulnerability.client.nvd.CveItem;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV2;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV20;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV30;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV30Data;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV31;
import io.github.jeremylong.openvulnerability.client.nvd.CvssV31Data;
import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import io.github.jeremylong.openvulnerability.client.nvd.LangString;
import io.github.jeremylong.openvulnerability.client.nvd.Metrics;
import io.github.jeremylong.openvulnerability.client.nvd.Node;
import io.github.jeremylong.openvulnerability.client.nvd.Reference;
import io.github.jeremylong.openvulnerability.client.nvd.Weakness;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.ExternalReference;
import org.cyclonedx.proto.v1_4.ScoreMethod;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityAffectedVersions;
import org.cyclonedx.proto.v1_4.VulnerabilityAffects;
import org.cyclonedx.proto.v1_4.VulnerabilityRating;
import org.hyades.resolver.CweResolver;
import org.hyades.vulnmirror.datasource.Datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hyades.vulnmirror.datasource.util.ParserUtil.mapSeverity;

/**
 * Parser and processor of NVD data feeds.
 */
public final class NvdToCyclonedxParser {

    public static class BovWrapper<T> {
        public final Bom bov;
        public final T object;

        public BovWrapper(Bom bov, T object) {
            this.bov = bov;
            this.object = object;
        }
    }

    // Using 'generic' as versioning scheme for NVD due to lack of package data
    private static final String UNI_VERS_RANGE = "vers:generic/";

    public static Bom parse(DefCveItem nvdVuln) {
        CveItem cveItem = nvdVuln.getCve();
        Vulnerability.Builder cdxVuln = Vulnerability.newBuilder()
                .setSource(Source.newBuilder().setName(Datasource.NVD.name()))
                .setId(cveItem.getId())
                .setDescription(parseDescription(cveItem.getDescriptions()))
                .addAllRatings(parseCveImpact(cveItem.getMetrics()));
        if (cveItem.getWeaknesses() != null) {
            cdxVuln.addAllCwes(parseCwes(cveItem.getWeaknesses()));
        }
        Optional.ofNullable(cveItem.getPublished())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(cdxVuln::setPublished);

        Optional.ofNullable(cveItem.getLastModified())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(cdxVuln::setUpdated);

        Bom cdxBom = Bom.newBuilder()
                .addAllExternalReferences(parseReferences(cveItem.getReferences()))
                .build();

        BovWrapper<List<VulnerabilityAffects>> bovWrapper = parseCpe(cdxBom, cveItem.getConfigurations());

        cdxVuln.addAllAffects(bovWrapper.object);
        Bom parsedCpeBom = bovWrapper.bov;

        return Bom.newBuilder(parsedCpeBom)
                .addVulnerabilities(cdxVuln.build())
                .build();
    }

    private static BovWrapper<List<VulnerabilityAffects>> parseCpe(Bom cdxBom, List<Config> configurations) {

        List<VulnerabilityAffects> affected = new ArrayList<>();
        AtomicReference<Bom> bovUpdated = new AtomicReference(cdxBom);
        if (configurations != null) {
            configurations.forEach(config -> {
                List<Node> nodes = config.getNodes();
                nodes.forEach(node -> {
                    List<CpeMatch> cpeMatches = node.getCpeMatch();
                    cpeMatches.forEach(cpeMatch -> {
                        if (cpeMatch.getVulnerable()) {

                            BovWrapper<VulnerabilityAffects> bovWrapper = parseVersionRangeAffected(bovUpdated.get(), cpeMatch);
                            affected.add(bovWrapper.object);
                            bovUpdated.set(bovWrapper.bov);
                        }
                    });
                });
            });
        }
        return new BovWrapper(bovUpdated.get(), affected);
    }

    private static BovWrapper<VulnerabilityAffects> parseVersionRangeAffected(Bom bom, CpeMatch cpeMatch) {

        AtomicReference<Bom> bovUpdated = new AtomicReference(bom);
        var vulnerabilityAffects = VulnerabilityAffects.newBuilder();
        BovWrapper<String> bovWrapper = getBomRef(bovUpdated.get(), cpeMatch.getCriteria());
        bovUpdated.set(bovWrapper.bov);
        vulnerabilityAffects.setRef(bovWrapper.object);

        String rangeSpecifier = "";
        var versionRange = VulnerabilityAffectedVersions.newBuilder();
        if (cpeMatch.getVersionStartIncluding() != null) {
            rangeSpecifier = cpeMatch.getVersionStartIncluding() + "|";
        }
        if (cpeMatch.getVersionStartExcluding() != null) {
            rangeSpecifier += cpeMatch.getVersionStartExcluding() + "|";
        }
        if (cpeMatch.getVersionEndIncluding() != null) {
            rangeSpecifier += cpeMatch.getVersionEndIncluding() + "|";
        }
        if (cpeMatch.getVersionEndExcluding() != null) {
            rangeSpecifier += cpeMatch.getVersionEndExcluding() + "|";
        }

        if (!StringUtils.isBlank(rangeSpecifier)) {
            versionRange.setRange(StringUtils.chop(UNI_VERS_RANGE + rangeSpecifier));
            vulnerabilityAffects.addVersions(versionRange);
        }
        return new BovWrapper(bovUpdated.get(), vulnerabilityAffects.build());
    }

    private static BovWrapper<String> getBomRef(Bom cdxBom, String cpe) {
        if (cdxBom.getComponentsList() != null) {
            Optional<Component> existingComponent = cdxBom.getComponentsList().stream().filter(c ->
                    c.getCpe().equalsIgnoreCase(cpe)).findFirst();
            if (existingComponent.isPresent()) {
                return new BovWrapper(cdxBom, existingComponent.get().getBomRef());
            }
        }
        UUID uuid = UUID.randomUUID();
        Component component = Component.newBuilder()
                .setBomRef(uuid.toString())
                .setCpe(cpe)
                .build();

        Bom.Builder cycloneDx = Bom.newBuilder(cdxBom)
                .addComponents(component);
        return new BovWrapper(cycloneDx.build(), uuid.toString());
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
                CvssV20 cvss = baseMetric.getCvssData();
                Optional.ofNullable(cvss).map(cvss20 -> VulnerabilityRating.newBuilder()
                        .setScore(cvss20.getBaseScore().doubleValue())
                        .setMethod(ScoreMethod.SCORE_METHOD_CVSSV2)
                        .setVector(cvss20.getVectorString())
                        .setSeverity(mapSeverity(baseMetric.getBaseSeverity()))
                        .build()).ifPresent(rating -> ratings.add(rating));
            });
        }

        // CVSS V30
        List<CvssV30> baseMetricV3 = metrics.getCvssMetricV30();
        if (CollectionUtils.isNotEmpty(baseMetricV3)) {
            baseMetricV3.forEach(baseMetric -> {
                CvssV30Data cvss = baseMetric.getCvssData();
                Optional.ofNullable(cvss).map(cvssx -> VulnerabilityRating.newBuilder()
                        .setScore(cvssx.getBaseScore().doubleValue())
                        .setMethod(ScoreMethod.SCORE_METHOD_CVSSV3)
                        .setVector(cvssx.getVectorString())
                        .setSeverity(mapSeverity(cvssx.getBaseSeverity().value()))
                        .build()).ifPresent(rating -> ratings.add(rating));
            });
        }

        // CVSS V31
        List<CvssV31> baseMetricV31 = metrics.getCvssMetricV31();
        if (CollectionUtils.isNotEmpty(baseMetricV31)) {
            baseMetricV31.forEach(baseMetric -> {
                CvssV31Data cvss = baseMetric.getCvssData();
                Optional.ofNullable(cvss).map(cvss31 -> VulnerabilityRating.newBuilder()
                        .setScore(cvss.getBaseScore().doubleValue())
                        .setMethod(ScoreMethod.SCORE_METHOD_CVSSV31)
                        .setVector(cvss.getVectorString())
                        .setSeverity(mapSeverity(cvss.getBaseSeverity().value()))
                        .build()).ifPresent(rating -> ratings.add(rating));
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
}
