package org.hyades.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyclonedx.model.Bom;
import org.hyades.osv.client.OsvClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hyades.util.FileUtil.deleteFileAndDir;

@ApplicationScoped
public class OsvMirrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsvMirrorHandler.class);
    private final OsvClient client;
    private final ObjectMapper objectMapper;

    @Inject
    public OsvMirrorHandler(final OsvClient client,
                            @Named("osvObjectMapper") ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public List<Bom> performMirror(String ecosystem) throws IOException {
        Path ecosystemZip = client.downloadEcosystemZip(ecosystem);
        List<Bom> osvAdvisories =  new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(ecosystemZip.toFile());
             ZipInputStream zipInput = new ZipInputStream(inputStream)) {
            osvAdvisories = unzipFolder(zipInput);
            deleteFileAndDir(ecosystemZip);
            LOGGER.info("OSV mirroring completed for ecosystem: {} advisories: {}",  ecosystem, osvAdvisories);
        } catch (IOException e) {
            LOGGER.error("Exception found while reading from OSV: ", e);
        }
        return osvAdvisories;
    }

    private List<Bom> unzipFolder(ZipInputStream zipIn) throws IOException {
        var osvAdvisories =  new ArrayList<Bom>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn));
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
                osvAdvisories.add(osvAdvisory);
            }
            zipEntry = zipIn.getNextEntry();
            reader = new BufferedReader(new InputStreamReader(zipIn));
        }
        reader.close();
        return osvAdvisories;
    }
}
