package org.acme.analyzer;

import org.acme.client.snyk.Issue;
import org.acme.client.snyk.ModelConverter;
import org.acme.client.snyk.Page;
import org.acme.client.snyk.PageData;
import org.acme.client.snyk.SnykClient;
import org.acme.model.Component;
import org.acme.model.VulnerabilityResult;
import org.acme.tasks.scanners.AnalyzerIdentity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SnykAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnykAnalyzer.class);

    private final SnykClient client;
    private final String apiToken;
    private final String apiOrgId;

    @Inject
    public SnykAnalyzer(@RestClient final SnykClient client,
                        @ConfigProperty(name = "scanner.snyk.token") final Optional<String> apiToken,
                        @ConfigProperty(name = "scanner.snyk.org.id") final Optional<String> apiOrgId) {
        this.client = client;
        this.apiToken = apiToken.map(token -> "token " + token).orElse(null);
        this.apiOrgId = apiOrgId.orElse(null);
    }

    public List<VulnerabilityResult> analyze(final Component component) {
        final String encodedCoordinates = URLEncoder.encode(component.getPurl().getCoordinates(), StandardCharsets.UTF_8);
        final Page<Issue> issuesPage = client.getIssues(apiToken, apiOrgId, encodedCoordinates, "2022-09-15");

        final var results = new ArrayList<VulnerabilityResult>();
        for (final PageData<Issue> data : issuesPage.data()) {
            if (!"issue".equals(data.type())) {
                LOGGER.warn("Skipping unexpected data type: {}", data.type());
                continue;
            }

            final var result = new VulnerabilityResult();
            result.setComponent(component);
            result.setIdentity(AnalyzerIdentity.SNYK_ANALYZER);
            result.setVulnerability(ModelConverter.convert(data.attributes()));
            results.add(result);
        }

        return results;
    }

}
