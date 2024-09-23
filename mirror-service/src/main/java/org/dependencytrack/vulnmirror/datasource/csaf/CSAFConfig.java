package org.dependencytrack.vulnmirror.datasource.csaf;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Provider;

@ApplicationScoped
public class CSAFConfig {

    private final Provider<Optional<Boolean>> enabledProvider;

    CSAFConfig(
        @ConfigProperty(name = "dtrack.vuln-source.csaf.enabled") final Provider<Optional<Boolean>> enabledProvider
    ) {
        this.enabledProvider = enabledProvider;
    }

    Optional<Boolean> enabled() {
        return enabledProvider.get();
    }
}
