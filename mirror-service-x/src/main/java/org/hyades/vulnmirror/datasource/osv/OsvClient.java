package org.hyades.vulnmirror.datasource.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.hyades.vulnmirror.datasource.util.FileUtil.getTempFileLocation;

/**
 * Client for the OSV REST API.
 */
@ApplicationScoped
public class OsvClient {
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final OsvConfig osvConfig;

    @Inject
    public OsvClient(@Named("osvHttpClient") final CloseableHttpClient httpClient,
                     @Named("osvObjectMapper") final ObjectMapper objectMapper, OsvConfig osvConfig) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.osvConfig = osvConfig;
    }

    public Path downloadEcosystemZip(String ecosystem) throws IOException {
        final var request = new HttpGet(this.osvConfig.baseurl() + "/" + URLEncoder.encode(ecosystem, StandardCharsets.UTF_8).replace("+", "%20")
                + "/all.zip");
        try (final CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Path tempFileLocation = getTempFileLocation(ecosystem, ".zip");
                    try (FileOutputStream outstream = new FileOutputStream(tempFileLocation.toFile())) {
                        entity.writeTo(outstream);
                        return tempFileLocation;
                    }
                }
            } else {
                throw new WebApplicationException(
                        "Unexpected response status: " + response.getStatusLine().getStatusCode() + " for ecosystem: " + ecosystem,
                        Response.status(response.getStatusLine().getStatusCode()).build());
            }
        }
        return null;
    }
}
