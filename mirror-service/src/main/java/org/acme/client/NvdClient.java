package org.acme.client;

import alpine.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.common.HttpClientPool;
import org.acme.commonnotification.NotificationConstants;
import org.acme.commonnotification.NotificationGroup;
import org.acme.commonnotification.NotificationScope;
import org.acme.model.Notification;
import org.acme.model.NotificationLevel;
import org.acme.model.NvdResourceType;
import org.acme.nvd.NvdToCyclonedxParser;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.cyclonedx.model.Bom;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static org.acme.util.FileUtil.close;
import static org.acme.util.FileUtil.writeTimeStampFile;

/**
 * Client for the NVD REST API.
 */
@ApplicationScoped
public class NvdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdClient.class);
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private boolean mirroredWithoutErrors = true;

    @Inject
    public NvdClient(@Named("mirrorHttpClient") final CloseableHttpClient httpClient,
                     @Named("mirrorObjectMapper") final ObjectMapper objectMapper,
                     @ConfigProperty(name = "mirror.nvd.base.url") final Optional<String> apiBaseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiBaseUrl = apiBaseUrl.orElse(null);
    }

    public List<Bom> downloadNvdFeed(final File outputDir, final String cveJsonUrl, final NvdResourceType nvdResourceType) {
        List<Bom> nvdVulnerabilities = new ArrayList<>();
        File file;
        try {
            String urlString = this.apiBaseUrl + cveJsonUrl + nvdResourceType;
            final URL url = new URL(urlString);
            String filename = url.getFile();
            filename = filename.substring(filename.lastIndexOf('/') + 1);
            file = new File(outputDir, filename).getAbsoluteFile();
            if (file.exists()) {
                long modificationTime = 0;
                File timestampFile = new File(outputDir, filename + ".ts");
                if(timestampFile.exists()) {
                    BufferedReader tsBufReader = new BufferedReader(new FileReader(timestampFile));
                    String text = tsBufReader.readLine();
                    modificationTime = Long.parseLong(text);
                }

                if (System.currentTimeMillis() < ((86400000 * 5) + modificationTime)) {
                    if (NvdResourceType.CVE_YEAR_DATA == nvdResourceType) {
                        LOGGER.info("Retrieval of " + filename + " not necessary. Will use modified feed for updates.");
                        return null;
                    } else if (NvdResourceType.CVE_META == nvdResourceType) {
                        return null;
                    } else if (NvdResourceType.CVE_MODIFIED_DATA == nvdResourceType) {
                        final long fileSize = checkHead(urlString);
                        if (file.length() == fileSize) {
                            LOGGER.info("Using cached version of " + filename);
                            return null;
                        }
                    }
                }
            }
            LOGGER.info("Initiating download of " + url.toExternalForm());
            final HttpUriRequest request = new HttpGet(urlString);
            try (final CloseableHttpResponse response = HttpClientPool.getClient().execute(request)) {
                final StatusLine status = response.getStatusLine();
                if (status.getStatusCode() == 200) {
                    LOGGER.info("Downloading...");
                    try (InputStream in = response.getEntity().getContent()) {
                        File temp = File.createTempFile(filename, null);
                        FileUtils.copyInputStreamToFile(in, temp);
                        Files.copy(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        Files.delete(temp.toPath());
                        if (NvdResourceType.CVE_YEAR_DATA == nvdResourceType || NvdResourceType.CVE_MODIFIED_DATA == nvdResourceType) {
                            // Sets the last modified date to 0. Upon a successful parse, it will be set back to its original date.
                            File timestampFile = new File(outputDir, filename + ".ts");
                            writeTimeStampFile(timestampFile, 0L);
                        }
                        if (file.getName().endsWith(".gz")) {
                            nvdVulnerabilities = uncompress(file, nvdResourceType);
                        }
                    }
                } else if (response.getStatusLine().getStatusCode() == 403) {
                    mirroredWithoutErrors = false;
                    final String detailMessage = "This may occur if the NVD is throttling connections due to excessive load or repeated " +
                            "connections from the same IP address or as a result of firewall or proxy authentication failures";
                    LOGGER.warn("Unable to download - HTTP Response 403: " + status.getReasonPhrase());
                    LOGGER.warn(detailMessage);
                    Notification.dispatch(new Notification()
                            .scope(NotificationScope.SYSTEM)
                            .group(NotificationGroup.DATASOURCE_MIRRORING)
                            .title(NotificationConstants.Title.NVD_MIRROR)
                            .content("An error occurred mirroring the contents of the National Vulnerability Database. Check log for details. HTTP Response: " + status.getStatusCode() + ". " + detailMessage)
                            .level(NotificationLevel.ERROR)
                    );
                } else {
                    mirroredWithoutErrors = false;
                    LOGGER.warn("Unable to download - HTTP Response " + status.getStatusCode() + ": " + status.getReasonPhrase());
                    Notification.dispatch(new Notification()
                            .scope(NotificationScope.SYSTEM)
                            .group(NotificationGroup.DATASOURCE_MIRRORING)
                            .title(NotificationConstants.Title.NVD_MIRROR)
                            .content("An error occurred mirroring the contents of the National Vulnerability Database. Check log for details. HTTP Response: " + status.getStatusCode())
                            .level(NotificationLevel.ERROR)
                    );
                }
            }
        } catch (IOException e) {
            mirroredWithoutErrors = false;
            LOGGER.error("Download failed : " + e.getMessage());
            Notification.dispatch(new Notification()
                    .scope(NotificationScope.SYSTEM)
                    .group(NotificationGroup.DATASOURCE_MIRRORING)
                    .title(NotificationConstants.Title.NVD_MIRROR)
                    .content("An error occurred mirroring the contents of the National Vulnerability Database. Check log for details. " + e.getMessage())
                    .level(NotificationLevel.ERROR)
            );
        }
        return nvdVulnerabilities;
    }

    /**
     * Performs a HTTP HEAD request to determine if a URL has updates since the last
     * time it was requested.
     * @param cveUrl the URL to perform a HTTP HEAD request on
     * @return the length of the content if it were to be downloaded
     */
    private long checkHead(final String cveUrl) {
        final HttpUriRequest request = new HttpHead(cveUrl);
        try (final CloseableHttpResponse response = this.httpClient.execute(request)) {
            return Long.valueOf(response.getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue());
        } catch (IOException | NumberFormatException | NullPointerException e) {
            LOGGER.error("Failed to determine content length");
        }
        return 0;
    }

    /**
     * Extracts a GZip file.
     * @param file the file to extract
     */
    private List<Bom> uncompress(final File file, final NvdResourceType nvdResourceType) {
        List<Bom> nvdVulnerabilities = new ArrayList<>();
        final byte[] buffer = new byte[1024];
        GZIPInputStream gzis = null;
        OutputStream out = null;
        try {
            LOGGER.info("Uncompressing " + file.getName());
            gzis = new GZIPInputStream(Files.newInputStream(file.toPath()));
            final File uncompressedFile = new File(file.getAbsolutePath().replaceAll(".gz", ""));
            out = Files.newOutputStream(uncompressedFile.toPath());
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            final long start = System.currentTimeMillis();
            if (NvdResourceType.CVE_YEAR_DATA == nvdResourceType || NvdResourceType.CVE_MODIFIED_DATA == nvdResourceType) {
                final NvdToCyclonedxParser parser = new NvdToCyclonedxParser();
                nvdVulnerabilities = parser.parse(uncompressedFile);
                // Update modification time
                File timestampFile = new File( file.getAbsolutePath() + ".ts");
                writeTimeStampFile(timestampFile, start);
            }
        } catch (IOException ex) {
            mirroredWithoutErrors = false;
            LOGGER.error("An error occurred uncompressing NVD payload", ex);
        } finally {
            close(gzis);
            close(out);
        }
        return nvdVulnerabilities;
    }
}
