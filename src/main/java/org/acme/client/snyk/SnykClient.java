package org.acme.client.snyk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.packageurl.PackageURL;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Client for the Snyk REST API.
 * <p>
 * Note: Using the MicroProfile RestClient does not work, because Snyk expects the PURL
 * to be encoded differently than what the MP RestClient is doing (it expects ":" and "@"
 * to be encoded, which is normally not necessary for URL path segments).
 */
@ApplicationScoped
public class SnykClient {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private final String apiOrgId;
    private final String apiToken;
    private final String apiVersion;

    @Inject
    public SnykClient(@Named("snykHttpClient") final CloseableHttpClient httpClient,
                      @Named("snykObjectMapper") final ObjectMapper objectMapper,
                      @ConfigProperty(name = "scanner.snyk.base.url") final Optional<String> apiBaseUrl,
                      @ConfigProperty(name = "scanner.snyk.org.id") final Optional<String> apiOrgId,
                      @ConfigProperty(name = "scanner.snyk.token") final Optional<String> apiToken,
                      @ConfigProperty(name = "scanner.snyk.api.version", defaultValue = "2022-09-15") final String apiVersion) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiBaseUrl = apiBaseUrl.orElse(null);
        this.apiOrgId = apiOrgId.orElse(null);
        this.apiToken = apiToken.orElse(null);
        this.apiVersion = apiVersion;
    }

    public Page<Issue> getIssues(final PackageURL purl) throws IOException {
        final String encodedPurl = URLEncoder.encode(purl.getCoordinates(), StandardCharsets.UTF_8);
        final var request = new HttpGet("%s/rest/orgs/%s/packages/%s/issues?version=%s".formatted(apiBaseUrl, apiOrgId, encodedPurl, apiVersion));
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + apiToken);
        request.setHeader(HttpHeaders.ACCEPT, "application/vnd.api+json");

        try (final CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return objectMapper.readValue(response.getEntity().getContent(), new TypeReference<>() {
                });
            } else {
                throw new WebApplicationException("Unexpected response status: " + response.getStatusLine().getStatusCode());
            }
        }
    }

}
