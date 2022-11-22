package org.acme.commonutil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public class SecretsUtil {
    private static File getKeyPath(KeyType keyType) {
        return new File("" + System.getProperty("user.home") + "/.dependency-track" + File.separator + "keys" + File.separator + keyType.name().toLowerCase() + ".key");
    }

    public static SecretKey loadSecretKey() throws IOException, ClassNotFoundException {
        File file = getKeyPath(KeyType.SECRET);
        InputStream fis = Files.newInputStream(file.toPath());

        SecretKey key;
        try {
            ObjectInputStream ois = new ObjectInputStream(fis);

            try {
                key = (SecretKey) ois.readObject();
            } catch (Exception var9) {
                try {
                    ois.close();
                } catch (Exception var8) {
                    var9.addSuppressed(var8);
                }

                throw var9;
            }

            ois.close();
        } catch (Exception var10) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception var7) {
                    var10.addSuppressed(var7);
                }
            }

            throw var10;
        }

        if (fis != null) {
            fis.close();
        }

        return key;

    }

    enum KeyType {
        PRIVATE,
        PUBLIC,
        SECRET;

        private KeyType() {
        }
    }

    public static byte[] encryptAsBytes(String plainText) throws Exception {
        SecretKey secretKey = loadSecretKey();
        return encryptAsBytes(secretKey, plainText);
    }


    public static byte[] encryptAsBytes(SecretKey secretKey, String plainText) throws Exception {
        byte[] clean = plainText.getBytes();
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, secretKey, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(clean);
        byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
        System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);
        return encryptedIVAndText;
    }
    public static String encryptAsString(String plainText) throws Exception {
        return Base64.getEncoder().encodeToString(encryptAsBytes(plainText));
    }

    public static String decryptAsString(String encryptedText) throws Exception {
        return new String(decryptAsBytes(Base64.getDecoder().decode(encryptedText)));
    }

    public static byte[] decryptAsBytes(byte[] encryptedIvTextBytes) throws Exception {
        SecretKey secretKey = loadSecretKey();
        return decryptAsBytes(secretKey, encryptedIvTextBytes);
    }


    public static byte[] decryptAsBytes(SecretKey secretKey, byte[] encryptedIvTextBytes) throws Exception {
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        int encryptedSize = encryptedIvTextBytes.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);
        Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDecrypt.init(2, secretKey, ivParameterSpec);
        return cipherDecrypt.doFinal(encryptedBytes);
    }

}

