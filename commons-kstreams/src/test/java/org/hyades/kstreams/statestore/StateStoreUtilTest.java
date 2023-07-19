package org.hyades.kstreams.statestore;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.apache.kafka.streams.state.internals.InMemoryKeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.internals.RocksDbKeyValueBytesStoreSupplier;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Suite
@SelectClasses(value = {
        StateStoreUtilTest.ConfigurableKeyValueStoreWithoutQuarkusConfigTest.class,
        StateStoreUtilTest.ConfigurableKeyValueStoreWitQuarkusConfigTest.class,
        StateStoreUtilTest.DefaultChangelogTopicConfigTest.class
})
class StateStoreUtilTest {

    static class ConfigurableKeyValueStoreWithoutQuarkusConfigTest {

        @Test
        void test() {
            assertThat(StateStoreUtil.configurableKeyValueStore("storeName"))
                    .isInstanceOf(InMemoryKeyValueBytesStoreSupplier.class);
        }

    }

    @QuarkusTest
    @TestProfile(ConfigurableKeyValueStoreWitQuarkusConfigTest.TestProfile.class)
    static class ConfigurableKeyValueStoreWitQuarkusConfigTest {

        public static class TestProfile implements QuarkusTestProfile {

            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "state-store.type", "rocks_db"
                );
            }
        }

        @Inject // Force injection, otherwise Quarkus will not discover the config mapping.
        @SuppressWarnings("unused")
        StateStoreConfig stateStoreConfig;

        @Test
        void test() {
            assertThat(StateStoreUtil.configurableKeyValueStore("storeName"))
                    .isInstanceOf(RocksDbKeyValueBytesStoreSupplier.class);
        }

    }

    static class DefaultChangelogTopicConfigTest {

        @Test
        void test() {
            assertThat(StateStoreUtil.defaultChangelogTopicConfig())
                    .containsExactlyInAnyOrderEntriesOf(Map.of(
                            "cleanup.policy", "compact",
                            "segment.bytes", "67108864",
                            "max.compaction.lag.ms", "0"
                    ));
        }

    }

}