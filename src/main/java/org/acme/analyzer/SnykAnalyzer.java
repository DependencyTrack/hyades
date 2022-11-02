package org.acme.analyzer;

import com.github.packageurl.PackageURL;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.acme.client.snyk.Issue;
import org.acme.client.snyk.ModelConverter;
import org.acme.client.snyk.Page;
import org.acme.client.snyk.PageData;
import org.acme.client.snyk.SnykClient;
import org.acme.model.Component;
import org.acme.model.VulnerabilityResult;
import org.acme.parser.common.resolver.CweResolver;
import org.acme.tasks.scanners.AnalyzerIdentity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApplicationScoped
public class SnykAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnykAnalyzer.class);
    private static final String API_VERSION = "2022-09-15";

    private final SnykClient client;
    private final RateLimiter rateLimiter;
    private final CweResolver cweResolver;
    private final String apiToken;
    private final String apiOrgId;

    @Inject
    public SnykAnalyzer(@RestClient final SnykClient client,
                        @Named("snykRateLimiter") final RateLimiter rateLimiter,
                        final CweResolver cweResolver,
                        @ConfigProperty(name = "scanner.snyk.token") final Optional<String> apiToken,
                        @ConfigProperty(name = "scanner.snyk.org.id") final Optional<String> apiOrgId) {
        this.client = client;
        this.rateLimiter = rateLimiter;
        this.cweResolver = cweResolver;
        this.apiToken = apiToken.map(token -> "token " + token).orElse(null);
        this.apiOrgId = apiOrgId.orElse(null);
    }

    public List<VulnerabilityResult> analyze(final List<Component> components) {
        return components.stream()
                .flatMap(component -> analyzeComponent(component).stream())
                .toList();
    }

    private List<VulnerabilityResult> analyzeComponent(final Component component) {
        final PackageURL purl = component.getPurl();

        final Supplier<Page<Issue>> rateLimitedRequest;
        if (purl.getNamespace() != null) {
            rateLimitedRequest = RateLimiter.decorateSupplier(rateLimiter,
                    () -> client.getIssues(apiToken, apiOrgId, purl.getType(), purl.getNamespace(), purl.getName(), purl.getVersion(), API_VERSION));
        } else {
            rateLimitedRequest = RateLimiter.decorateSupplier(rateLimiter,
                    () -> client.getIssues(apiToken, apiOrgId, purl.getType(), purl.getName(), purl.getVersion(), API_VERSION));
        }

        final Page<Issue> issuesPage = rateLimitedRequest.get();
        if (issuesPage.data() == null || issuesPage.data().isEmpty()) {
            final var result = new VulnerabilityResult();
            result.setComponent(component);
            result.setIdentity(AnalyzerIdentity.SNYK_ANALYZER);
            result.setVulnerability(null);
            return List.of(result);
        }

        final var results = new ArrayList<VulnerabilityResult>();
        for (final PageData<Issue> data : issuesPage.data()) {
            if (!"issue".equals(data.type())) {
                LOGGER.warn("Skipping unexpected data type: {}", data.type());
                continue;
            }

            final var result = new VulnerabilityResult();
            result.setComponent(component);
            result.setIdentity(AnalyzerIdentity.SNYK_ANALYZER);
            result.setVulnerability(ModelConverter.convert(cweResolver, data.attributes()));
            results.add(result);
        }

        return results;
    }

}
