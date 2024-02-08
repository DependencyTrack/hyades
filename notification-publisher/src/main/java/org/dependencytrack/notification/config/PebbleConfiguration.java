package org.dependencytrack.notification.config;

import io.pebbletemplates.pebble.PebbleEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import org.dependencytrack.notification.template.extension.CustomExtension;

class PebbleConfiguration {

    @Produces
    @ApplicationScoped
    @Named("pebbleEngineJson")
    PebbleEngine pebbleEngineJson(final CustomExtension customExtension) {
        return new PebbleEngine.Builder()
                .extension(customExtension)
                .defaultEscapingStrategy("json")
                .build();
    }

    @Produces
    @ApplicationScoped
    @Named("pebbleEnginePlainText")
    PebbleEngine pebbleEnginePlainText(final CustomExtension customExtension) {
        return new PebbleEngine.Builder()
                .extension(customExtension)
                .newLineTrimming(false)
                .build();
    }

    @Produces
    @ApplicationScoped
    CustomExtension pebbleEngineExtension() {
        return new CustomExtension();
    }

}
