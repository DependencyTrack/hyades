package org.dependencytrack.repometaanalyzer.serde;

import com.github.packageurl.PackageURL;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaPurlSerde implements Serde<PackageURL> {

    private final Serializer<PackageURL> serializer = new KafkaPurlSerializer();
    private final Deserializer<PackageURL> deserializer = new KafkaPurlDeserializer();

    @Override
    public Serializer<PackageURL> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<PackageURL> deserializer() {
        return deserializer;
    }

}
