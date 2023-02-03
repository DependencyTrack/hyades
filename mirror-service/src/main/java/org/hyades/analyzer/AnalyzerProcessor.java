package org.hyades.analyzer;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.hyades.model.VulnerabilityScanKey;
import org.hyades.model.VulnerabilityScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hyades.commonutil.VulnerabilityUtil.normalizedCvssV2Score;
import static org.hyades.commonutil.VulnerabilityUtil.normalizedCvssV3Score;
import static org.hyades.commonutil.VulnerabilityUtil.trimSummary;

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

            if (analyzerVuln.getVulnerableVersions() != null) {
                parseAffects(vulnerability, analyzerVuln);
            }

            // TODO parse aliases
//            var aliases = new ArrayList<Vulnerability.Reference>();
//            analyzerVuln.getAliases().stream().forEach(vulnerabilityAlias -> {
//                var alias = new Vulnerability.Reference();
//            });

            cyclonedxBom.setVulnerabilities(List.of(vulnerability));
            context().forward(record
                    .withKey(analyzer + "/" + vulnerability.getId())
                    .withValue(cyclonedxBom));
        });
        LOGGER.info("Vulnerabilities analyzed by "+ analyzer +" completed successfully.");
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

    private static void parseAffects(Vulnerability vulnerability, org.hyades.model.Vulnerability analyzerVuln) {
        // TODO verify the range parsing.
        var affected = new Vulnerability.Affect();
        var version = new Vulnerability.Version();
        version.setVersion(analyzerVuln.getVulnerableVersions());
        affected.setVersions(List.of(version));
        vulnerability.setAffects(List.of(affected));
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
