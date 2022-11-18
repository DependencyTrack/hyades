import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
System.out.println("Generating secret key");
var keyGen = KeyGenerator.getInstance("AES");
var random = SecureRandom.getInstance("SHA1PRNG");
keyGen.init(256, random);
var secretKey = keyGen.generateKey();
System.out.println("Writing key to secret.key");
try (var fos = Files.newOutputStream(Paths.get(System.getProperty("secret.key.destination")));
     var oos = new ObjectOutputStream(fos)) {
oos.writeObject(secretKey);
}
/exit