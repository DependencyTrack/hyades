package org.acme.client;

import alpine.common.logging.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Client for the Snyk REST API.
 * <p>
 * Note: Using the MicroProfile RestClient does not work, because Snyk expects the PURL
 * to be encoded differently than what the MP RestClient is doing (it expects ":" and "@"
 * to be encoded, which is normally not necessary for URL path segments).
 */
@ApplicationScoped
public class OsvClient {

    private static final Logger LOGGER = Logger.getLogger(OsvClient.class);
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private final List<String> ecosystems;

    @Inject
    public OsvClient(@Named("osvHttpClient") final CloseableHttpClient httpClient,
                     @Named("osvObjectMapper") final ObjectMapper objectMapper,
                     @ConfigProperty(name = "mirror.osv.base.url") final Optional<String> apiBaseUrl,
                     @ConfigProperty(name = "mirror.osv.ecosystems") final Optional<String> ecosystems) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiBaseUrl = apiBaseUrl.orElse(null);
        this.ecosystems = ecosystems.isPresent() ? Arrays.stream(ecosystems.get().split(";")).map(String::trim).toList() : null;
    }

    public List<String> getEcosystems() {
        return this.ecosystems;
    }

    public InputStream getEcosystemZip(String ecosystem) throws IOException {
        final var request = new HttpGet(this.apiBaseUrl + URLEncoder.encode(ecosystem, StandardCharsets.UTF_8).replace("+", "%20")
                + "/all.zip");
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
