package org.acme.analyzer;

import alpine.common.util.Pageable;
import org.acme.client.ossindex.ComponentReport;
import org.acme.client.ossindex.ComponentReportRequest;
import org.acme.client.ossindex.ComponentReportVulnerability;
import org.acme.client.ossindex.ModelConverter;
import org.acme.client.ossindex.OssIndexClient;
import org.acme.model.Component;
import org.acme.model.Vulnerability;
import org.acme.tasks.scanners.AnalyzerIdentity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OssIndexAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OssIndexAnalyzer.class);

    private final OssIndexClient client;
    private final String apiAuth;

    @Inject
    public OssIndexAnalyzer(@RestClient final OssIndexClient client,
                            @ConfigProperty(name = "scanner.ossindex.api.username") final Optional<String> apiUsername,
                            @ConfigProperty(name = "scanner.ossindex.api.token") final Optional<String> apiToken) {
        this.client = client;
        if (apiUsername.isPresent() && apiToken.isPresent()) {
            this.apiAuth = "Basic " + Base64.getEncoder().encodeToString("%s:%s".formatted(apiUsername.get(), apiToken.get()).getBytes());
        } else {
            this.apiAuth = null;
        }
    }

    public List<AnalysisResult> analyze(final List<Component> components) {
        final var purlComponents = new MultivaluedHashMap<String, Component>();
        for (final Component component : components) {
            purlComponents.add(component.getPurl().getCoordinates(), component);
        }

        final var results = new ArrayList<AnalysisResult>();

        final Pageable<String> paginatedPurls = new Pageable<>(128, new ArrayList<>(purlComponents.keySet()));
        while (!paginatedPurls.isPaginationComplete()) {
            final List<ComponentReport> reports;
            if (apiAuth == null) {
                reports = client.getComponentReports(new ComponentReportRequest(paginatedPurls.getPaginatedList()));
            } else {
                reports = client.getComponentReports(apiAuth, new ComponentReportRequest(paginatedPurls.getPaginatedList()));
            }

            for (final ComponentReport report : reports) {
                final List<Component> affectedComponents = purlComponents.get(report.coordinates());
                if (affectedComponents == null) {
                    LOGGER.warn("Reported PURL does not match any components: " + report.coordinates());
                    continue;
                }

                if (report.vulnerabilities().isEmpty()) {
                    for (final Component component : affectedComponents) {
                        results.add(new AnalysisResult(component, null, AnalyzerIdentity.OSSINDEX_ANALYZER));
                    }
                    continue;
                }

                for (final ComponentReportVulnerability reportedVuln : report.vulnerabilities()) {
                    final Vulnerability vuln = ModelConverter.convert(reportedVuln);

                    for (final Component component : affectedComponents) {
                        results.add(new AnalysisResult(component, vuln, AnalyzerIdentity.OSSINDEX_ANALYZER));
                    }
                }
            }

            paginatedPurls.nextPage();
        }

        return results;
    }

}
