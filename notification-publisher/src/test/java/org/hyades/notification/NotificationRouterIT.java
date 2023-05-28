package org.hyades.notification;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hyades.common.KafkaTopic;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.notification.v1.Group;
import org.hyades.proto.notification.v1.Level;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.Scope;
import org.hyades.util.WireMockTestResource;
import org.hyades.util.WireMockTestResource.InjectWireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@QuarkusIntegrationTest
@TestProfile(NotificationRouterIT.TestProfile.class)
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(WireMockTestResource.class)
public class NotificationRouterIT {

    public static class TestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("client.http.config.proxy-timeout-socket", "2");
        }

    }

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    @InjectWireMock
    WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() throws Exception {
        try (final Connection connection = DriverManager.getConnection(
                ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String.class),
                ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class),
                ConfigProvider.getConfig().getValue("quarkus.datasource.password", String.class))) {
            connection.createStatement().execute("""
                    INSERT INTO "NOTIFICATIONPUBLISHER" ("ID", "DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID") VALUES
                    (1, true, 'foo', 'org.hyades.notification.publisher.WebhookPublisher', 'template', 'text/plain', '1781db56-51a8-462a-858c-6030a2341dfc');
                    """);
            connection.createStatement().execute("""
                    INSERT INTO "NOTIFICATIONRULE" ("ID", "ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "NOTIFICATION_LEVEL", "SCOPE", "UUID", "PUBLISHER_CONFIG") VALUES
                    (1, true, 'foo', 1, 'NEW_VULNERABILITY', false, 'INFORMATIONAL', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62', '{"destination": "http://localhost:%d/foo"}');
                    """.formatted(wireMockServer.port()));
        }
    }

    @Test
    void test() {
        wireMockServer.stubFor(post(urlPathEqualTo("/foo"))
                .inScenario("notification-delivery")
                .willReturn(aResponse()
                        .withStatus(204)
                        .withFixedDelay(5 * 1000))
                .willSetStateTo("first-attempt-timeout"));

        wireMockServer.stubFor(post(urlPathEqualTo("/foo"))
                .inScenario("notification-delivery")
                .whenScenarioStateIs("first-attempt-timeout")
                .willReturn(aResponse()
                        .withStatus(204)));

        final var notification = Notification.newBuilder()
                .setScope(Scope.SCOPE_PORTFOLIO)
                .setLevel(Level.LEVEL_INFORMATIONAL)
                .setGroup(Group.GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();

        kafkaCompanion
                .produce(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .fromRecords(new ProducerRecord<>(KafkaTopic.NOTIFICATION_NEW_VULNERABILITY.getName(), "", notification));

        Awaitility.await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> wireMockServer.verify(2, postRequestedFor(urlPathEqualTo("/foo"))));
    }

}