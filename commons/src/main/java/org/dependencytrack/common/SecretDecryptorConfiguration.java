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
package org.dependencytrack.common;

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
