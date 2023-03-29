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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hyades.vulnmirror.datasource.util.FileUtil.deleteFileAndDir;

@ApplicationScoped
public class OsvMirror extends AbstractDatasourceMirror<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsvMirror.class);
    private final ExecutorService executorService;
    private final OsvClient client;
    private final ObjectMapper objectMapper;

    public OsvMirror(@Named("osvExecutorService") ExecutorService executorService, OsvClient client,
                     @Named("osvObjectMapper") ObjectMapper objectMapper, final MirrorStateStore mirrorStateStore,
                     final VulnerabilityDigestStore vulnDigestStore,
                     final Producer<String, byte[]> bovProducer) {
        super(Datasource.OSV, mirrorStateStore, vulnDigestStore, bovProducer, Void.class);
        this.executorService = executorService;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public void performMirror() {
        try {
            Path ecosystemZip = client.downloadEcosystemZip(Datasource.OSV.name());
            try (InputStream inputStream = new FileInputStream(ecosystemZip.toFile());
                 ZipInputStream zipInput = new ZipInputStream(inputStream)) {
                parseZipInputAndPublishIfChanged(zipInput);
                deleteFileAndDir(ecosystemZip);
                LOGGER.info("OSV mirroring completed for ecosystem: {}", Datasource.OSV);
            }
        } catch (IOException e) {
            LOGGER.error("Exception found while reading from OSV: ", e);
        }
    }

    private void parseZipInputAndPublishIfChanged(ZipInputStream zipIn) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn));
        try {
            ZipEntry zipEntry = zipIn.getNextEntry();
            while (zipEntry != null) {

                String line = null;
                StringBuilder out = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                var json = new JSONObject(out.toString());
                Bom osvAdvisory = new OsvToCyclonedxParser(this.objectMapper).parse(json);
                if (osvAdvisory != null) {
                    publishIfChanged(osvAdvisory);
                }
                zipEntry = zipIn.getNextEntry();
                reader = new BufferedReader(new InputStreamReader(zipIn));
            }
            reader.close();
        } catch (Exception ex) {
            LOGGER.error("An error occurred while performing OSV mirroring", ex);
        }
    }

    @Override
    public void doMirror() {
        executorService.submit(this::performMirror);
    }

}
