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

import org.junit.jupiter.api.BeforeEach;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

abstract class AbstractPskSecretKeyHandlerTest {

    private SecretKey secretKey;

    @BeforeEach
    void beforeEach() throws Exception {
        final byte[] encodedKey = Files.readAllBytes(Paths.get("src/test/resources/PskSecretKeysHandler/secret.key"));
        secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    /**
     * Encrypts the specified string using AES-256.
     * <p>
     * Ported from Alpine's {@code DataEncryption} class.
     *
     * @param text the text to encrypt
     * @return the encrypted string
     * @throws Exception a number of exceptions may be thrown
     * @see <a href="https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/main/java/alpine/security/crypto/DataEncryption.java">Alpine DataEncryption</a>
     */
    public String encryptAsString(final String text) throws Exception {
        return Base64.getEncoder().encodeToString(encryptAsBytes(text));
    }

    /**
     * Encrypts the specified plainText using AES-256.
     * <p>
     * Ported from Alpine's {@code DataEncryption} class.
     *
     * @param plainText the text to encrypt
     * @return the encrypted bytes
     * @throws Exception a number of exceptions may be thrown
     * @see <a href="https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/main/java/alpine/security/crypto/DataEncryption.java">Alpine DataEncryption</a>
     */
    byte[] encryptAsBytes(final String plainText) throws Exception {
        final byte[] clean = plainText.getBytes();

        // Generating IV
        int ivSize = 16;
        final byte[] iv = new byte[ivSize];
        final SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Encrypt
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        final byte[] encrypted = cipher.doFinal(clean);

        // Combine IV and encrypted parts
        final byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);

        return encryptedIVAndText;
    }

}
