package org.dependencytrack.repometaanalyzer.serde;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

public class KafkaPurlDeserializer implements Deserializer<PackageURL> {

    @Override
    public PackageURL deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return new PackageURL(new String(data, StandardCharsets.UTF_8));
        } catch (MalformedPackageURLException e) {
            throw new SerializationException(e);
        }
    }

}
