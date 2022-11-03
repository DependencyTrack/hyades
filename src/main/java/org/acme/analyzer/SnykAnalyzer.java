package org.acme.analyzer;

import alpine.security.crypto.DataEncryption;
import com.github.packageurl.PackageURL;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.acme.client.snyk.Issue;
import org.acme.client.snyk.ModelConverter;
import org.acme.client.snyk.Page;
import org.acme.client.snyk.PageData;
import org.acme.client.snyk.SnykClient;
import org.acme.model.Component;
import org.acme.model.ConfigPropertyConstants;
import org.acme.model.VulnerabilityResult;
import org.acme.parser.common.resolver.CweResolver;
import org.acme.persistence.QueryManager;
import org.acme.tasks.scanners.AnalyzerIdentity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
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
    private String apiToken;
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
        QueryManager queryManager = new QueryManager();
        final alpine.model.ConfigProperty apiTokenProperty = queryManager.getConfigProperty(
                ConfigPropertyConstants.SCANNER_SNYK_API_TOKEN.getGroupName(),
                ConfigPropertyConstants.SCANNER_SNYK_API_TOKEN.getPropertyName()
        );
        if (apiTokenProperty == null || apiTokenProperty.getPropertyValue() == null) {
            LOGGER.error("Please provide API token for use with Snyk");
        }
        try {
            apiToken = "token " + DataEncryption.decryptAsString(loadSecretKey(), apiTokenProperty.getPropertyValue());
        }catch (Exception ex){
            LOGGER.error("Error has occurred");
        }
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
    private File getKeyPath(SnykAnalyzer.KeyType keyType) {
        //File var10002 = Config.getInstance().getDataDirectorty();
        return new File("" + System.getProperty("user.home")+"/.dependency-track" + File.separator + "keys" + File.separator + keyType.name().toLowerCase() + ".key");
    }
    public SecretKey loadSecretKey()throws IOException, ClassNotFoundException {
            File file = this.getKeyPath(SnykAnalyzer.KeyType.SECRET);
            InputStream fis = Files.newInputStream(file.toPath());

            SecretKey key;
            try {
                ObjectInputStream ois = new ObjectInputStream(fis);

                try {
                    key = (SecretKey)ois.readObject();
                } catch (Throwable var9) {
                    try {
                        ois.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }

                    throw var9;
                }

                ois.close();
            } catch (Throwable var10) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                    }
                }

                throw var10;
            }

            if (fis != null) {
                fis.close();
            }

            return key;

    }
    static enum KeyType {
        PRIVATE,
        PUBLIC,
        SECRET;

        private KeyType() {
        }
    }
}
