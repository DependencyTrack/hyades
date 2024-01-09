package org.dependencytrack.repometaanalyzer.serde;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serde;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class KafkaPurlSerdeTest {

    private final Serde<PackageURL> purlSerde = new KafkaPurlSerde();

    @Test
    void testSerialize() throws MalformedPackageURLException {
        final byte[] data = purlSerde.serializer().serialize("topic", new PackageURL("pkg:foo/bar@1.2.3"));
        assertThat(data).asString(StandardCharsets.UTF_8).isEqualTo("pkg:foo/bar@1.2.3");
    }

    @Test
    void testDeserialize() {
        final PackageURL purl = purlSerde.deserializer().deserialize("topic", "pkg:foo/bar/baz@1.2.3".getBytes());
        assertThat(purl.getType()).isEqualTo("foo");
        assertThat(purl.getNamespace()).isEqualTo("bar");
        assertThat(purl.getName()).isEqualTo("baz");
        assertThat(purl.getVersion()).isEqualTo("1.2.3");
    }

    @Test
    void testDeserializeWithInvalidPurl() {
        assertThatExceptionOfType(SerializationException.class)
                .isThrownBy(() -> purlSerde.deserializer().deserialize("topic", "foo".getBytes()));
    }

}