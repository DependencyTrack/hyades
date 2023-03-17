package org.hyades.serde;

import com.github.packageurl.PackageURL;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class KafkaPurlSerializer implements Serializer<PackageURL> {

    @Override
    public byte[] serialize(final String topic, final PackageURL data) {
        if (data == null) {
            return null;
        }

        return data.canonicalize().getBytes(StandardCharsets.UTF_8);
    }

}
