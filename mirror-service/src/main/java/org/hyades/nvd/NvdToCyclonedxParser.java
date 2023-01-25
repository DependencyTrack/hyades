package org.hyades.nvd;

import io.github.jeremylong.nvdlib.nvd.Config;
import io.github.jeremylong.nvdlib.nvd.CpeMatch;
import io.github.jeremylong.nvdlib.nvd.CveItem;
import io.github.jeremylong.nvdlib.nvd.CvssV2;
import io.github.jeremylong.nvdlib.nvd.CvssV20;
import io.github.jeremylong.nvdlib.nvd.CvssV30;
import io.github.jeremylong.nvdlib.nvd.CvssV30__1;
import io.github.jeremylong.nvdlib.nvd.CvssV31;
import io.github.jeremylong.nvdlib.nvd.CvssV31__1;
import io.github.jeremylong.nvdlib.nvd.LangString;
import io.github.jeremylong.nvdlib.nvd.Metrics;
import io.github.jeremylong.nvdlib.nvd.Node;
import io.github.jeremylong.nvdlib.nvd.Reference;
import io.github.jeremylong.nvdlib.nvd.Weakness;
import org.acme.resolver.CweResolver;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.vulnerability.Vulnerability;

import java.sql.Date;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Parser and processor of NVD data feeds.
 */
public final class NvdToCyclonedxParser {

    private final Bom cdxBom = new Bom();
    private final Vulnerability cdxVuln = new Vulnerability();

    public Bom parse(CveItem nvdVuln) {

        // Source
        var source = new Vulnerability.Source();
        source.setName(org.acme.model.Vulnerability.Source.NVD.name());
        this.cdxVuln.setSource(source);

        // Vulnerability ID
        this.cdxVuln.setId(nvdVuln.getId());

        // Published and Modified dates
        if(nvdVuln.getPublished() != null) {
            this.cdxVuln.setPublished(
                    Date.from(nvdVuln.getPublished().toInstant(
                            ZoneId.systemDefault().getRules().getOffset(nvdVuln.getPublished())))
            );
        }
        if(nvdVuln.getPublished() != null) {
            this.cdxVuln.setUpdated(
                    Date.from(nvdVuln.getLastModified().toInstant(
                            ZoneId.systemDefault().getRules().getOffset(nvdVuln.getLastModified())))
            );
        }

        // Description
        this.cdxVuln.setDescription(parseDescription(nvdVuln.getDescriptions()));

        // References
        this.cdxBom.setExternalReferences(parseReferences(nvdVuln.getReferences()));

        // CWEs
        this.cdxVuln.setCwes(parseCwes(nvdVuln.getWeaknesses()));

        // CVSS
        if (nvdVuln.getMetrics() != null) {
            this.cdxVuln.setRatings(parseCveImpact(nvdVuln.getMetrics()));
        }

        // CPE
        this.cdxVuln.setAffects(parseCpe(nvdVuln.getConfigurations()));
        this.cdxBom.setVulnerabilities(List.of(cdxVuln));
        return this.cdxBom;
    }

    private List<Vulnerability.Affect> parseCpe(List<Config> configurations) {
        List<Vulnerability.Affect> affected = new ArrayList<>();
        configurations.forEach(config -> {
            List<Node> nodes = config.getNodes();
            nodes.forEach(node -> {
                List<CpeMatch> cpeMatches = node.getCpeMatch();
                cpeMatches.forEach(cpeMatch -> {
                    if(cpeMatch.getVulnerable()) {

                        var versionRangeAffected = new Vulnerability.Affect();
                        versionRangeAffected.setRef(getBomRef(cpeMatch.getCriteria()));

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
                        affected.add(versionRangeAffected);
                    }
                });
            });
        });
        return affected;
    }

    private String getBomRef(String cpe) {
        if (this.cdxBom.getComponents() != null) {
            Optional<Component> existingComponent = this.cdxBom.getComponents().stream().filter(c ->
                    c.getCpe().equalsIgnoreCase(cpe)).findFirst();
            if (existingComponent.isPresent()) {
                return existingComponent.get().getBomRef();
            }
        }
        var component = new Component();
        UUID uuid = UUID.randomUUID();
        component.setBomRef(uuid.toString());
        component.setCpe(cpe);
        this.cdxBom.addComponent(component);
        return uuid.toString();
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
                CvssV30__1 cvss = baseMetric.getCvssData();
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
                CvssV31__1 cvss = baseMetric.getCvssData();
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
