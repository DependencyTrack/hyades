package org.acme.analyzer;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SnykAnalyzer implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnykAnalyzer.class);

    private final SnykClient client;
    private final RateLimiter rateLimiter;
    private final CweResolver cweResolver;
    private final boolean isEnabled;

    @Inject
    public SnykAnalyzer(final SnykClient client,
                        @Named("snykRateLimiter") final RateLimiter rateLimiter,
                        final CweResolver cweResolver,
                        @ConfigProperty(name = "scanner.snyk.enabled", defaultValue = "false") final boolean isEnabled) {
        this.client = client;
        this.rateLimiter = rateLimiter;
        this.cweResolver = cweResolver;
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public List<VulnerabilityResult> analyze(final List<Component> components) {
        return components.stream()
                .flatMap(component -> analyzeComponent(component).stream())
                .toList();
    }

    private List<VulnerabilityResult> analyzeComponent(final Component component) {
        final Page<Issue> issuesPage;
        try {
            issuesPage = rateLimiter.executeCheckedSupplier(() -> client.getIssues(component.getPurl()));
        } catch (Throwable e) {
            // TODO: Handle analyzer errors properly
            throw new RuntimeException(e);
        }

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
