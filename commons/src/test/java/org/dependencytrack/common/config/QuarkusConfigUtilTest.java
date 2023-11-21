package org.dependencytrack.common.config;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.dependencytrack.config.HttpClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Suite
@SelectClasses(value = {
        QuarkusConfigUtilTest.WithoutQuarkusConfigTest.class,
        QuarkusConfigUtilTest.WithQuarkusConfigTest.class
})
class QuarkusConfigUtilTest {

    static class WithoutQuarkusConfigTest {

        @Test
        void test() {
            Assertions.assertThat(QuarkusConfigUtil.getConfigMapping(HttpClientConfig.class)).isNotPresent();
        }

    }

    @QuarkusTest
    @TestProfile(WithQuarkusConfigTest.TestProfile.class)
    static class WithQuarkusConfigTest {

        public static class TestProfile implements QuarkusTestProfile {

            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "client.http.config.max-total-connections", "666"
                );
            }
        }

        @Inject // Force injection, otherwise Quarkus will not discover the config mapping.
        @SuppressWarnings("unused")
        HttpClientConfig httpClientConfig;

        @Test
        void test() {
            assertThat(QuarkusConfigUtil.getConfigMapping(HttpClientConfig.class))
                    .isPresent().get()
                    .extracting(HttpClientConfig::maxTotalConnections)
                    .isEqualTo(666);
        }

    }

}