package org.hyades.common;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

public class SecretDecryptor {

    private final SecretKey secretKey;

    SecretDecryptor(final SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Decrypts the specified bytes using AES-256.
     * <p>
     * Ported from Alpine's {@code DataEncryption} class.
     *
     * @param encryptedIvTextBytes the text to decrypt
     * @return the decrypted bytes
     * @throws Exception a number of exceptions may be thrown
     * @see <a href="https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/main/java/alpine/security/crypto/DataEncryption.java">Alpine DataEncryption</a>
     */
    public byte[] decryptAsBytes(final byte[] encryptedIvTextBytes) throws Exception {
        int ivSize = 16;

        // Extract IV
        final byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Extract encrypted bytes
        final int encryptedSize = encryptedIvTextBytes.length - ivSize;
        final byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);

        // Decrypt
        final Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        return cipherDecrypt.doFinal(encryptedBytes);
    }

    /**
     * Decrypts the specified string using AES-256. The encryptedText is
     * expected to be the Base64 encoded representation of the encrypted bytes.
     * <p>
     * Ported from Alpine's {@code DataEncryption} class.
     *
     * @param encryptedText the text to decrypt
     * @return the decrypted string
     * @throws Exception a number of exceptions may be thrown
     * @see <a href="https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/main/java/alpine/security/crypto/DataEncryption.java">Alpine DataEncryption</a>
     */
    public String decryptAsString(final String encryptedText) throws Exception {
        return new String(decryptAsBytes(Base64.getDecoder().decode(encryptedText)));
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
