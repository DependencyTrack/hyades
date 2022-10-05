package org.acme;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.acme.model.Cwe;
import org.acme.persistence.CweImporter;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@QuarkusMain
@ApplicationScoped
public class Main {
    public static void main(String... args) {
        Quarkus.run(MyApp.class, args);
    }

    public static HashMap<Integer, String> cweInfo = new HashMap<>();

    public static class MyApp implements QuarkusApplication {
        Logger logger = Logger.getLogger("poc");

        @Override
        public int run(String... args) {
            try {
                Map<Integer, String> cweList = new CweImporter().processCweDefinitions();
                for (Map.Entry<Integer, String> entry : cweList.entrySet()) {
                    if (entry != null) {
                        cweInfo.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception ex) {
                logger.error("Error adding CWEs to database");
                logger.error(ex.getMessage());
            }
            Quarkus.waitForExit();
            return 0;
        }
    }
}