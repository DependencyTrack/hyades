/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.config;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.SecretKeysHandler;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.function.Predicate.not;

class PskSecretKeysHandlerFactory implements io.smallrye.config.SecretKeysHandlerFactory {

    @Override
    public io.smallrye.config.SecretKeysHandler getSecretKeysHandler(final ConfigSourceContext context) {
        // Using a LazySecretKeysHandler, such that we only attempt to load the secret key
        // when we're actually trying to decrypt something. Not every service will make use
        // of the secret key, hence we won't make the key available to those services.
        return new LazySecretKeysHandler(new io.smallrye.config.SecretKeysHandlerFactory() {
            @Override
            public SecretKeysHandler getSecretKeysHandler(final ConfigSourceContext context) {
                final String secretKeyPath = Optional.ofNullable(context.getValue("secret.key.path"))
                        .map(ConfigValue::getValue)
                        .filter(not(String::isBlank))
                        .orElse(null);
                if (secretKeyPath == null) {
                    throw new NoSuchElementException("""
                            Decryption with %s was requested, but no secret.key.path \
                            is configured""".formatted(PskSecretKeysHandler.NAME));
                }

                final SecretKey secretKey;
                try {
                    secretKey = readSecretKey(secretKeyPath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read secret key from %s".formatted(secretKeyPath), e);
                }

                return new PskSecretKeysHandler(secretKey);
            }

            @Override
            public String getName() {
                return PskSecretKeysHandler.NAME;
            }
        });
    }

    @Override
    public String getName() {
        return PskSecretKeysHandler.NAME;
    }

    private static SecretKey readSecretKey(final String secretKeyPath) throws IOException {
        try (final InputStream fis = Files.newInputStream(Paths.get(secretKeyPath))) {
            final byte[] encodedKey = fis.readAllBytes();
            return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        }
    }

}
