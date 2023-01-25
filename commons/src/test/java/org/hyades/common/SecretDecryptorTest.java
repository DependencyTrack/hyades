package org.hyades.common;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;


@QuarkusTest
@TestProfile(SecretDecryptorTest.TestProfile.class)
class SecretDecryptorTest {
    public static class TestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "client.http.config.proxy-timeout-connection", "20",
                    "client.http.config.proxy-timeout-pool", "40",
                    "client.http.config.proxy-timeout-socket", "20"
            );
        }
    }

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