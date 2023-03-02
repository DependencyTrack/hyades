package org.hyades.nvd;

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
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.hyades.resolver.CweResolver;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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

        Bom cdxBom = new Bom();
        final Vulnerability cdxVuln = new Vulnerability();

        // Source
        var source = new Vulnerability.Source();
        source.setName(org.hyades.model.Vulnerability.Source.NVD.name());
        cdxVuln.setSource(source);

        // Vulnerability ID
        cdxVuln.setId(nvdVuln.getId());

        // Published and Modified dates
        if(nvdVuln.getPublished() != null) {
            cdxVuln.setPublished(Date.from(nvdVuln.getPublished().toInstant()));
        }
        if(nvdVuln.getPublished() != null) {
            cdxVuln.setUpdated(Date.from(nvdVuln.getLastModified().toInstant()));
        }

        // Description
        cdxVuln.setDescription(parseDescription(nvdVuln.getDescriptions()));

        // References
        cdxBom.setExternalReferences(parseReferences(nvdVuln.getReferences()));

        // CWEs
        cdxVuln.setCwes(parseCwes(nvdVuln.getWeaknesses()));

        // CVSS
        if (nvdVuln.getMetrics() != null) {
            cdxVuln.setRatings(parseCveImpact(nvdVuln.getMetrics()));
        }

        // CPE
        BovWrapper<List<Vulnerability.Affect>> bovWrapper = parseCpe(cdxBom, nvdVuln.getConfigurations());
        cdxVuln.setAffects(bovWrapper.object);
        cdxBom = bovWrapper.bov;
        cdxBom.setVulnerabilities(List.of(cdxVuln));
        return cdxBom;
    }

    private static BovWrapper<List<Vulnerability.Affect>> parseCpe(Bom cdxBom, List<Config> configurations) {

        List<Vulnerability.Affect> affected = new ArrayList<>();
        AtomicReference<Bom> bovUpdated = new AtomicReference(cdxBom);

        configurations.forEach(config -> {
            List<Node> nodes = config.getNodes();
            nodes.forEach(node -> {
                List<CpeMatch> cpeMatches = node.getCpeMatch();
                cpeMatches.forEach(cpeMatch -> {
                    if(cpeMatch.getVulnerable()) {

                        BovWrapper<Vulnerability.Affect> bovWrapper = parseVersionRangeAffected(bovUpdated.get(), cpeMatch);
                        affected.add(bovWrapper.object);
                        bovUpdated.set(bovWrapper.bov);
                    }
                });
            });
        });
        return new BovWrapper(bovUpdated.get(), affected);
    }

    private static BovWrapper<Vulnerability.Affect> parseVersionRangeAffected(Bom bom, CpeMatch cpeMatch) {

        AtomicReference<Bom> bovUpdated = new AtomicReference(bom);
        var versionRangeAffected = new Vulnerability.Affect();
        BovWrapper<String> bovWrapper = getBomRef(bovUpdated.get(), cpeMatch.getCriteria());
        bovUpdated.set(bovWrapper.bov);
        versionRangeAffected.setRef(bovWrapper.object);

        String uniVersionRange = "vers:"+cpeMatch.getCriteria()+"/";
        var versionRange = new Vulnerability.Version();
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
        versionRangeAffected.setVersions(List.of(versionRange));
        return new BovWrapper(bovUpdated.get(), versionRangeAffected);
    }

    private static BovWrapper<String> getBomRef(Bom cdxBom, String cpe) {
        if (cdxBom.getComponents() != null) {
            Optional<Component> existingComponent = cdxBom.getComponents().stream().filter(c ->
                    c.getCpe().equalsIgnoreCase(cpe)).findFirst();
            if (existingComponent.isPresent()) {
                return new BovWrapper(cdxBom, existingComponent.get().getBomRef());
            }
        }
        var component = new Component();
        UUID uuid = UUID.randomUUID();
        component.setBomRef(uuid.toString());
        component.setCpe(cpe);
        cdxBom.addComponent(component);
        return new BovWrapper(cdxBom, uuid.toString());
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

    private static List<Vulnerability.Rating> parseCveImpact(Metrics metrics) {
        List<Vulnerability.Rating> ratings = new ArrayList<>();

        // CVSS V2
        List<CvssV2> baseMetricV2 = metrics.getCvssMetricV2();
        if (baseMetricV2 != null && baseMetricV2.size() > 0) {
            baseMetricV2.forEach(baseMetric -> {
                CvssV20 cvss = baseMetric.getCvssData();
                if (cvss != null) {
                    var rating = new Vulnerability.Rating();
                    rating.setScore(cvss.getBaseScore());
                    rating.setMethod(Vulnerability.Rating.Method.CVSSV2);
                    rating.setVector(cvss.getVectorString());
                    rating.setSeverity(Vulnerability.Rating.Severity.fromString(baseMetric.getBaseSeverity().toLowerCase()));
                    ratings.add(rating);
                }
            });
        }

        // CVSS V30
        List<CvssV30> baseMetricV3 = metrics.getCvssMetricV30();
        if (baseMetricV3 != null && baseMetricV3.size() > 0) {
            baseMetricV3.forEach(baseMetric -> {
                CvssV30Data cvss = baseMetric.getCvssData();
                if (cvss != null) {
                    var rating = new Vulnerability.Rating();
                    rating.setScore(cvss.getBaseScore());
                    rating.setMethod(Vulnerability.Rating.Method.CVSSV3);
                    rating.setVector(cvss.getVectorString());
                    rating.setSeverity(Vulnerability.Rating.Severity.fromString(cvss.getBaseSeverity().value()));
                    ratings.add(rating);
                }
            });
        }

        // CVSS V31
        List<CvssV31> baseMetricV31 = metrics.getCvssMetricV31();
        if (baseMetricV31 != null && baseMetricV31.size() > 0) {
            baseMetricV31.forEach(baseMetric -> {
                CvssV31Data cvss = baseMetric.getCvssData();
                if (cvss != null) {
                    var rating = new Vulnerability.Rating();
                    rating.setScore(cvss.getBaseScore());
                    rating.setMethod(Vulnerability.Rating.Method.CVSSV3);
                    rating.setVector(cvss.getVectorString());
                    rating.setSeverity(Vulnerability.Rating.Severity.fromString(cvss.getBaseSeverity().value()));
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
        references.forEach(reference -> {
            var externalReference = new ExternalReference();
            externalReference.setUrl(reference.getUrl());
            externalReferences.add(externalReference);
        });
        return externalReferences;
    }
}
