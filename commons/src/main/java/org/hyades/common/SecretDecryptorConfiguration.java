package org.hyades.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class SecretDecryptorConfiguration {

    @Produces
    @ApplicationScoped
    SecretDecryptor secretDecryptor(final SecretKey secretKey) {
        return new SecretDecryptor(secretKey);
    }

    @Produces
    @ApplicationScoped
    SecretKey secretKey(@ConfigProperty(name = "secret.key.path", defaultValue = "${HOME}/.dependency-track/keys/secret.key") final String secretKeyPath) throws IOException {
        try (final InputStream fis = Files.newInputStream(Paths.get(secretKeyPath))) {
            final byte[] encodedKey = fis.readAllBytes();
            return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        }
    }

}
