package org.acme.osv;

import javassist.bytecode.analysis.Analyzer;
import kong.unirest.json.JSONObject;
import org.acme.client.OsvClient;
import org.acme.model.OsvAdvisory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ApplicationScoped
public class OsvAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsvAnalyzer.class);

    @Inject
    private final OsvClient client;

    private final boolean isEnabled;

    @Inject
    public OsvAnalyzer(final OsvClient client,
                        @ConfigProperty(name = "mirror.osv.enabled", defaultValue = "false") final boolean isEnabled) {
        this.client = client;
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void performMirror() throws IOException {
        List<String> ecosystems = client.getEcosystems();
        if (ecosystems != null && !ecosystems.isEmpty()) {
            for (String ecosystem : ecosystems) {
                try (InputStream inputStream = client.getEcosystemZip(ecosystem);
                     ZipInputStream zipInput = new ZipInputStream(inputStream)) {
                    unzipFolder(zipInput);
                }
            }
        } else {
            LOGGER.info("Google OSV mirroring is disabled. No ecosystem selected.");
        }
    }

    private void unzipFolder(ZipInputStream zipIn) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn));
        OsvAdvisoryParser parser = new OsvAdvisoryParser();
        ZipEntry zipEntry = zipIn.getNextEntry();
        while (zipEntry != null) {

            String line = null;
            StringBuilder out = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            JSONObject json = new JSONObject(out.toString());
            final OsvAdvisory osvAdvisory = parser.parse(json);
            if (osvAdvisory != null) {
                // TODO: publish each advisory to topic dtrack.vulnerability from here?
            }
            zipEntry = zipIn.getNextEntry();
            reader = new BufferedReader(new InputStreamReader(zipIn));
        }
        reader.close();
    }
}
