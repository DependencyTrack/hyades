package org.hyades.vulnmirror.datasource.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_4.Bom;
import org.hyades.vulnmirror.datasource.AbstractDatasourceMirror;
import org.hyades.vulnmirror.datasource.Datasource;
import org.hyades.vulnmirror.state.MirrorStateStore;
import org.hyades.vulnmirror.state.VulnerabilityDigestStore;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hyades.proto.notification.v1.Level.LEVEL_ERROR;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
public class OsvMirror extends AbstractDatasourceMirror<Void> {

    private static final String NOTIFICATION_TITLE = "OSV Mirroring";
    private static final Logger LOGGER = LoggerFactory.getLogger(OsvMirror.class);
    private final ExecutorService executorService;
    private final OsvClient client;
    private final ObjectMapper objectMapper;
    private final OsvConfig osvConfig;

    public OsvMirror(@Named("osvExecutorService") ExecutorService executorService, OsvClient client,
                     @Named("osvObjectMapper") ObjectMapper objectMapper, final MirrorStateStore mirrorStateStore,
                     final VulnerabilityDigestStore vulnDigestStore,
                     final Producer<String, byte[]> bovProducer, OsvConfig osvConfig) {
        super(Datasource.OSV, mirrorStateStore, vulnDigestStore, bovProducer, Void.class);
        this.executorService = executorService;
        this.client = client;
        this.objectMapper = objectMapper;
        this.osvConfig = osvConfig;
    }

    public void performMirror(String ecosystem) throws IOException, ExecutionException, InterruptedException {
        Path ecosystemZip = client.downloadEcosystemZip(ecosystem);
        try (InputStream inputStream = Files.newInputStream(ecosystemZip, StandardOpenOption.DELETE_ON_CLOSE);
             ZipInputStream zipInput = new ZipInputStream(inputStream)) {
            parseZipInputAndPublishIfChanged(zipInput);
        }

    }

    private void parseZipInputAndPublishIfChanged(ZipInputStream zipIn) throws IOException, ExecutionException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn, StandardCharsets.UTF_8));
        ZipEntry zipEntry = zipIn.getNextEntry();
        while (zipEntry != null) {

            String line = null;
            StringBuilder out = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            var json = new JSONObject(out.toString());
            Bom bov = new OsvToCyclonedxParser(this.objectMapper).parse(json, this.osvConfig.aliasSyncEnabled());
            if (bov != null) {
                publishIfChanged(bov);
            }
            zipEntry = zipIn.getNextEntry();
            reader = new BufferedReader(new InputStreamReader(zipIn));
        }
        reader.close();

    }

    @Override
    public Future<?> doMirror(String ecosystems) {
        return executorService.submit(() -> {
            if (ecosystems != null) {
                for (String ecosystem : ecosystems.split(",")) {
                    try {
                        performMirror(ecosystem);
                        dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                                "OSV mirroring completed for ecosystem: " + ecosystem);
                    } catch (InterruptedException e) {
                        LOGGER.warn("Thread was interrupted", e);
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        LOGGER.error("An unexpected error occurred mirroring the contents of ecosystem:" + ecosystem, e);
                        dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                                "An error occurred mirroring the contents of ecosystem :" + ecosystem + " for OSV. Check log for details.");
                    }
                }
            } else {
                LOGGER.error("Ecosystem was passed as null");
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "Tried to mirror null ecosystem for OSV.");
            }
        });
    }

}
