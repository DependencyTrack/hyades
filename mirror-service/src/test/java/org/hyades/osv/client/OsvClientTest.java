package org.hyades.osv.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpStatus;
import org.hyades.osv.OsvMirrorHandler;
import org.hyades.util.WireMockTestResource;
import org.hyades.util.WireMockTestResource.InjectWireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hyades.util.FileUtil.deleteFileAndDir;

@QuarkusTest
@QuarkusTestResource(
        value = WireMockTestResource.class,
        initArgs = @ResourceArg(name = "serverUrlProperty", value = "mirror.osv.base.url")
)
class OsvClientTest {

    @InjectWireMock
    WireMockServer wireMockServer;

    @Inject
    OsvClient osvClient;

    @Inject
    OsvMirrorHandler osvMirrorHandler;

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @Test
    void testDownloadOsvZips() throws Exception {
        // Have the OSV API return one zip with one file for the provided ecosystem
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withBody(getOsvTestZipData("osv-vuln.json"))
                        .withStatus(HttpStatus.SC_OK)));
        final Path osvZipFilePath = osvClient.downloadEcosystemZip("Go");
        assertThat(osvZipFilePath).isNotNull();
        deleteFileAndDir(osvZipFilePath);
        assertThat(!Files.exists(osvZipFilePath));
   }

    @ParameterizedTest
    @ValueSource(ints = {
            HttpStatus.SC_NOT_MODIFIED,
            HttpStatus.SC_BAD_REQUEST,
            HttpStatus.SC_NOT_FOUND,
            HttpStatus.SC_TOO_MANY_REQUESTS,
            HttpStatus.SC_INTERNAL_SERVER_ERROR
    })
    void testGetIssuesWithUnexpectedResponse(final int responseCode) {
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(responseCode)));

        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> osvClient.downloadEcosystemZip("Maven"))
                .satisfies(exception -> {
                    assertThat(exception.getResponse()).isNotNull();
                    assertThat(exception.getResponse().getStatus()).isEqualTo(responseCode);
                });
    }

    private byte[] getOsvTestZipData(final String name) throws IOException {
        final URL resourceUrl = getClass().getClassLoader().getResource(Paths.get("osv", name).toString());
        assertThat(resourceUrl).isNotNull();

        try (final InputStream inputStream = resourceUrl.openStream()) {
            return inputStream.readAllBytes();
        }
    }

}