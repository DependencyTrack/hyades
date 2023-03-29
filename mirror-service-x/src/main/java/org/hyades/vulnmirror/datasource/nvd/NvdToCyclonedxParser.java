package org.hyades.vulnmirror.datasource.nvd;

import com.google.protobuf.Timestamp;
import io.github.jeremylong.nvdlib.nvd.Config;
import io.github.jeremylong.nvdlib.nvd.CpeMatch;
import io.github.jeremylong.nvdlib.nvd.CveItem;
import io.github.jeremylong.nvdlib.nvd.CvssV2;
import io.github.jeremylong.nvdlib.nvd.CvssV20;
import io.github.jeremylong.nvdlib.nvd.CvssV30;
import io.github.jeremylong.nvdlib.nvd.CvssV30Data;
import io.github.jeremylong.nvdlib.nvd.CvssV31;
import io.github.jeremylong.nvdlib.nvd.CvssV31Data;
import io.github.jeremylong.nvdlib.nvd.LangString;
import io.github.jeremylong.nvdlib.nvd.Metrics;
import io.github.jeremylong.nvdlib.nvd.Node;
import io.github.jeremylong.nvdlib.nvd.Reference;
import io.github.jeremylong.nvdlib.nvd.Weakness;
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

    public static Bom parse(CveItem nvdVuln) {

        Vulnerability.Builder cdxVuln = Vulnerability.newBuilder()
                .setSource(Source.newBuilder().setName(Datasource.NVD.name()))
                .setId(nvdVuln.getId())
                .setDescription(parseDescription(nvdVuln.getDescriptions()))
                .addAllCwes(parseCwes(nvdVuln.getWeaknesses()))
                .addAllRatings(parseCveImpact(nvdVuln.getMetrics()));

        Optional.ofNullable(nvdVuln.getPublished())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(cdxVuln::setPublished);

        Optional.ofNullable(nvdVuln.getLastModified())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(cdxVuln::setUpdated);

        Bom cdxBom = Bom.newBuilder()
                .addAllExternalReferences(parseReferences(nvdVuln.getReferences()))
                .build();

        BovWrapper<List<VulnerabilityAffects>> bovWrapper = parseCpe(cdxBom, nvdVuln.getConfigurations());

        cdxVuln.addAllAffects(bovWrapper.object);
        Bom parsedCpeBom = bovWrapper.bov;

        return Bom.newBuilder(parsedCpeBom)
                .addVulnerabilities(cdxVuln.build())
                .build();
    }

    private static BovWrapper<List<VulnerabilityAffects>> parseCpe(Bom cdxBom, List<Config> configurations) {

        List<VulnerabilityAffects> affected = new ArrayList<>();
        AtomicReference<Bom> bovUpdated = new AtomicReference(cdxBom);

        configurations.forEach(config -> {
            List<Node> nodes = config.getNodes();
            nodes.forEach(node -> {
                List<CpeMatch> cpeMatches = node.getCpeMatch();
                cpeMatches.forEach(cpeMatch -> {
                    if(cpeMatch.getVulnerable()) {

                        BovWrapper<VulnerabilityAffects> bovWrapper = parseVersionRangeAffected(bovUpdated.get(), cpeMatch);
                        affected.add(bovWrapper.object);
                        bovUpdated.set(bovWrapper.bov);
                    }
                });
            });
        });
        return new BovWrapper(bovUpdated.get(), affected);
    }

    private static BovWrapper<VulnerabilityAffects> parseVersionRangeAffected(Bom bom, CpeMatch cpeMatch) {

        AtomicReference<Bom> bovUpdated = new AtomicReference(bom);
        var versionRangeAffected = VulnerabilityAffects.newBuilder();
        BovWrapper<String> bovWrapper = getBomRef(bovUpdated.get(), cpeMatch.getCriteria());
        bovUpdated.set(bovWrapper.bov);
        versionRangeAffected.setRef(bovWrapper.object);

        String uniVersionRange = "vers:"+cpeMatch.getCriteria()+"/";
        var versionRange = VulnerabilityAffectedVersions.newBuilder();
        if(cpeMatch.getVersionStartIncluding() != null) {
            uniVersionRange += cpeMatch.getVersionStartIncluding() + "|";
        }
        if(cpeMatch.getVersionStartExcluding() != null) {
            uniVersionRange += cpeMatch.getVersionStartExcluding() + "|";
        }
        if(cpeMatch.getVersionEndIncluding() != null) {
            uniVersionRange += cpeMatch.getVersionEndIncluding() + "|";
        }
        if(cpeMatch.getVersionEndExcluding() != null) {
            uniVersionRange += cpeMatch.getVersionEndExcluding() + "|";
        }

        versionRange.setRange(StringUtils.chop(uniVersionRange));
        versionRangeAffected.addVersions(versionRange);
        return new BovWrapper(bovUpdated.get(), versionRangeAffected.build());
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
                if (cvss != null) {
                    var rating = VulnerabilityRating.newBuilder()
                            .setScore(cvss.getBaseScore())
                            .setMethod(ScoreMethod.SCORE_METHOD_CVSSV2)
                            .setVector(cvss.getVectorString())
                            .setSeverity(mapSeverity(baseMetric.getBaseSeverity()))
                            .build();
                    ratings.add(rating);
                }
            });
        }

        // CVSS V30
        List<CvssV30> baseMetricV3 = metrics.getCvssMetricV30();
        if (CollectionUtils.isNotEmpty(baseMetricV3)) {
            baseMetricV3.forEach(baseMetric -> {
                CvssV30Data cvss = baseMetric.getCvssData();
                if (cvss != null) {
                    var rating = VulnerabilityRating.newBuilder()
                            .setScore(cvss.getBaseScore())
                            .setMethod(ScoreMethod.SCORE_METHOD_CVSSV3)
                            .setVector(cvss.getVectorString())
                            .setSeverity(mapSeverity(cvss.getBaseSeverity().value()))
                            .build();
                    ratings.add(rating);
                }
            });
        }

        // CVSS V31
        List<CvssV31> baseMetricV31 = metrics.getCvssMetricV31();
        if (CollectionUtils.isNotEmpty(baseMetricV31)) {
            baseMetricV31.forEach(baseMetric -> {
                CvssV31Data cvss = baseMetric.getCvssData();
                if (cvss != null) {
                    var rating = VulnerabilityRating.newBuilder()
                            .setScore(cvss.getBaseScore())
                            .setMethod(ScoreMethod.SCORE_METHOD_CVSSV31)
                            .setVector(cvss.getVectorString())
                            .setSeverity(mapSeverity(cvss.getBaseSeverity().value()))
                            .build();
                    ratings.add(rating);
                }
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
        references.forEach(reference ->
                externalReferences.add(ExternalReference.newBuilder()
                    .setUrl(reference.getUrl())
                    .build()));
        return externalReferences;
    }
}
