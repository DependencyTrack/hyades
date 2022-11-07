package org.acme.util;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;

public class SecretsUtil {
    private File getKeyPath(KeyType keyType) {
        return new File("" + System.getProperty("user.home") + "/.dependency-track" + File.separator + "keys" + File.separator + keyType.name().toLowerCase() + ".key");
    }

    public SecretKey loadSecretKey() throws IOException, ClassNotFoundException {
        File file = this.getKeyPath(KeyType.SECRET);
        InputStream fis = Files.newInputStream(file.toPath());

        SecretKey key;
        try {
            ObjectInputStream ois = new ObjectInputStream(fis);

            try {
                key = (SecretKey) ois.readObject();
            } catch (Throwable var9) {
                try {
                    ois.close();
                } catch (Throwable var8) {
                    var9.addSuppressed(var8);
                }

                throw var9;
            }

            ois.close();
        } catch (Throwable var10) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable var7) {
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

    static enum KeyType {
        PRIVATE,
        PUBLIC,
        SECRET;

        private KeyType() {
        }
    }
}

