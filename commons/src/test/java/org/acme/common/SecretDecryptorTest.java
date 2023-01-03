package org.acme.common;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
class SecretDecryptorTest {

    @Inject
    SecretDecryptor secretDecryptor;

    /**
     * Ported from Alpine's {@code DataEncryptionTest}.
     *
     * @throws Exception When an exception occurred during test execution
     * @see <a href="https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/test/java/alpine/security/crypto/DataEncryptionTest.java">Alpine DataEncryptionTest</a>
     */
    @Test
    void encryptAndDecryptAsBytes1Test() throws Exception {
        byte[] bytes = secretDecryptor.encryptAsBytes("This is encrypted text");
        Assertions.assertTrue(bytes.length > 0);
        Assertions.assertEquals("This is encrypted text", new String(secretDecryptor.decryptAsBytes(bytes)));
    }

    /**
     * Ported from Alpine's {@code DataEncryptionTest}.
     *
     * @throws Exception When an exception occurred during test execution
     * @see <a href="https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/test/java/alpine/security/crypto/DataEncryptionTest.java">Alpine DataEncryptionTest</a>
     */
    @Test
    void encryptAndDecryptAsBytes2Test() throws Exception {
        byte[] bytes = secretDecryptor.encryptAsBytes("This is encrypted text");
        Assertions.assertTrue(bytes.length > 0);
        Assertions.assertEquals("This is encrypted text", new String(secretDecryptor.decryptAsBytes(bytes)));
    }

}