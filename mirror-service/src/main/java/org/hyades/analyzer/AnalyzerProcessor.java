package org.hyades.analyzer;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.hyades.model.VulnerabilityAlias;
import org.hyades.model.VulnerabilityScanKey;
import org.hyades.model.VulnerabilityScanResult;
import org.hyades.model.VulnerableSoftware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.hyades.commonutil.VulnerabilityUtil.normalizedCvssV2Score;
import static org.hyades.commonutil.VulnerabilityUtil.normalizedCvssV3Score;
import static org.hyades.commonutil.VulnerabilityUtil.trimSummary;
import static org.hyades.util.ParserUtil.getBomRefIfComponentExists;

public class AnalyzerProcessor extends ContextualProcessor<VulnerabilityScanKey, VulnerabilityScanResult, String, Bom> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerProcessor.class);

    public AnalyzerProcessor() {
    }

    @Override
    public void init(ProcessorContext<String, Bom> context) {
        super.init(context);
    }

    @Override
    public void process(Record<VulnerabilityScanKey, VulnerabilityScanResult> record) {

        var analyzerVulns = record.value().vulnerabilities();
        String analyzer = record.value().scanner().name();

        analyzerVulns.forEach(analyzerVuln -> {

            var cyclonedxBom = new Bom();
            var vulnerability = instantiateVulnerability(analyzerVuln);

            if (analyzerVuln.getSource() != null) {
                var source = new Vulnerability.Source();
                source.setName(analyzerVuln.getSource());
                vulnerability.setSource(source);
            }
            if (analyzerVuln.getComponents() != null) {
                parseComponents(cyclonedxBom, analyzerVuln.getComponents());
            }
            if (analyzerVuln.getCredits() != null) {
                parseCredits(vulnerability, analyzerVuln);
            }
            parseRatings(vulnerability, analyzerVuln);
            if (analyzerVuln.getVulnerableSoftware() != null) {
                parseAffects(cyclonedxBom, vulnerability, analyzerVuln.getVulnerableSoftware());
            }
            if (analyzerVuln.getAliases() != null) {
                parseAliases(vulnerability, analyzerVuln.getAliases());
            }
            cyclonedxBom.setVulnerabilities(List.of(vulnerability));
            context().forward(record
                    .withKey(analyzer + "/" + vulnerability.getId())
                    .withValue(cyclonedxBom));
        });
        LOGGER.info("Vulnerabilities analyzed by "+ analyzer +" completed successfully.");
    }

    private void parseAliases(Vulnerability vulnerability, List<VulnerabilityAlias> analyzerAliases) {
        var distinctAliases = new HashSet<Vulnerability.Reference>();
        analyzerAliases.stream().forEach(analyzerAlias -> {
            analyzerAlias.getAllBySource().forEach((aliasSource, aliasId) -> {
                var reference = new Vulnerability.Reference();
                var referenceSource = new Vulnerability.Source();
                referenceSource.setName(aliasSource.name());
                reference.setSource(referenceSource);
                reference.setId(aliasId);
                distinctAliases.add(reference);
            });
        });
        vulnerability.setReferences(distinctAliases.stream().toList());
    }

    private static void parseComponents(Bom cyclonedxBom, List<org.hyades.model.Component> components) {
        components.forEach(component -> {
            var cdxComponent = new Component();
            cdxComponent.setName(component.getName());
            cdxComponent.setVersion(component.getVersion());
            cdxComponent.setPurl(component.getPurl());
            cdxComponent.setCpe(component.getCpe());
            cdxComponent.setGroup(component.getGroup());
            cdxComponent.setBomRef(component.getBomRef());
            cyclonedxBom.addComponent(cdxComponent);
        });
    }

    private static Vulnerability instantiateVulnerability(org.hyades.model.Vulnerability analyzerVuln) {
        var vulnerability = new Vulnerability();
        vulnerability.setId(analyzerVuln.getVulnId());
        vulnerability.setDescription(analyzerVuln.getTitle());
        vulnerability.setDetail(trimSummary(analyzerVuln.getDetail()));
        vulnerability.setCreated(analyzerVuln.getCreated());
        vulnerability.setUpdated(analyzerVuln.getUpdated());
        vulnerability.setPublished(analyzerVuln.getPublished());
        vulnerability.setCwes(analyzerVuln.getCwes());
        return vulnerability;
    }

    private static void parseAffects(Bom cyclonedxBom, Vulnerability vulnerability, List<VulnerableSoftware> vulnerableSoftwares) {

        // Map one vulnerableSoftware to one CDX affected object
        var affectedPAckages = new ArrayList<Vulnerability.Affect>();
        vulnerableSoftwares.forEach(vulnerableSoftware -> {

            if (vulnerableSoftware.isVulnerable()) {
                var affected = new Vulnerability.Affect();
                PackageURL packageUrl = null;
                String packageType = null;

                // map bom reference
                if (vulnerableSoftware.getPurl() != null) {
                    try {
                        packageUrl = new PackageURL(vulnerableSoftware.getPurl());
                        packageType = packageUrl.getType();
                    } catch (MalformedPackageURLException ex) {
                        LOGGER.info("Error while parsing purl: {}", vulnerableSoftware.getPurl(), ex);
                    }
                    String bomReference = getBomRefIfComponentExists(cyclonedxBom, vulnerableSoftware.getPurl());
                    if (bomReference == null) {
                        Component component = createNewComponentWithPurl(vulnerableSoftware, packageUrl);
                        cyclonedxBom.addComponent(component);
                        bomReference = component.getBomRef();
                    }
                    affected.setRef(bomReference);
                }
                // map version ranges
                var versionRange = new Vulnerability.Version();
                if (vulnerableSoftware.getVersion() != null) {
                    versionRange.setVersion(vulnerableSoftware.getVersion());
                }
                String uniVersionRange = "vers:";
                if (packageType != null) {
                    uniVersionRange += packageType;
                }
                uniVersionRange += "/";
                if (vulnerableSoftware.getVersionStartIncluding() != null) {
                    uniVersionRange += ">=" + vulnerableSoftware.getVersionStartIncluding() + "|";
                }
                if (vulnerableSoftware.getVersionStartExcluding() != null) {
                    uniVersionRange += ">" + vulnerableSoftware.getVersionStartExcluding() + "|";
                }
                if (vulnerableSoftware.getVersionEndIncluding() != null) {
                    uniVersionRange += "<=" + vulnerableSoftware.getVersionEndIncluding() + "|";
                }
                if (vulnerableSoftware.getVersionEndExcluding() != null) {
                    uniVersionRange += "<" + vulnerableSoftware.getVersionEndExcluding() + "|";
                }
                versionRange.setRange(StringUtils.chop(uniVersionRange));
                affected.setVersions(List.of(versionRange));
                affectedPAckages.add(affected);
            }
        });
        vulnerability.setAffects(affectedPAckages);
    }

    private static Component createNewComponentWithPurl(VulnerableSoftware vulnerableSoftware, PackageURL packageUrl) {
        var component = new Component();
        UUID uuid = UUID.randomUUID();
        component.setBomRef(uuid.toString());
        if (packageUrl != null) {
            component.setName(packageUrl.getName());
            component.setPurl(vulnerableSoftware.getPurl());
            component.setVersion(packageUrl.getVersion());
        }
        return component;
    }

    private static void parseRatings(Vulnerability vulnerability, org.hyades.model.Vulnerability analyzerVuln) {
        var ratings = new ArrayList<Vulnerability.Rating>();
        if (analyzerVuln.getCvssV3Vector() != null) {
            var rating = new Vulnerability.Rating();
            rating.setMethod(Vulnerability.Rating.Method.CVSSV3);
            rating.setVector(analyzerVuln.getCvssV3Vector());
            rating.setScore(analyzerVuln.getCvssV3BaseScore().doubleValue());
            rating.setSeverity(Vulnerability.Rating.Severity.fromString(
                    String.valueOf(normalizedCvssV3Score(analyzerVuln.getCvssV3BaseScore().doubleValue()))));
            ratings.add(rating);
        }
        if (analyzerVuln.getCvssV2Vector() != null) {
            var rating = new Vulnerability.Rating();
            rating.setMethod(Vulnerability.Rating.Method.CVSSV2);
            rating.setVector(analyzerVuln.getCvssV2Vector());
            rating.setScore(analyzerVuln.getCvssV2BaseScore().doubleValue());
            rating.setSeverity(Vulnerability.Rating.Severity.fromString(
                    String.valueOf(normalizedCvssV2Score(analyzerVuln.getCvssV2BaseScore().doubleValue()))));
            ratings.add(rating);
        }
        vulnerability.setRatings(ratings);
    }

    private static void parseCredits(Vulnerability vulnerability, org.hyades.model.Vulnerability analyzerVuln) {
        var credits = new Vulnerability.Credits();
        var credit = new OrganizationalContact();
        credit.setName(analyzerVuln.getCredits());
        credits.setIndividuals(List.of(credit));
        vulnerability.setCredits(credits);
    }
}
