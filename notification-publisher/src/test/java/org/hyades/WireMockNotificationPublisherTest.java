package org.hyades;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.hyades.common.KafkaTopic;
import org.hyades.proto.KafkaProtobufSerializer;
import org.hyades.proto.notification.v1.Group;
import org.hyades.proto.notification.v1.Level;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.Scope;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@QuarkusTest
public class WireMockNotificationPublisherTest {

    @Inject
    EntityManager entityManager;

    @Inject
    Topology topology;

    private static WireMockServer wireMockServer;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Notification> inputTopic;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
    }

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.NOTIFICATION_NEW_VULNERABILITY.getName(),
                new StringSerializer(), new KafkaProtobufSerializer<>());
    }

    @AfterEach
    void afterEach() {
        WireMock.reset();
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @TestTransaction
    public void testPublisher() {
        final var notification = Notification.newBuilder()
                .setScope(Scope.SCOPE_PORTFOLIO)
                .setLevel(Level.LEVEL_INFORMATIONAL)
                .setGroup(Group.GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();

        stubFor(get("/instance-dsl").willReturn(ok()));
        final var publisherConfig = "{\"destination\":\"" + wireMockServer.url("/instance-dsl") + "\"}";

        final var publisherId = (BigInteger) entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONPUBLISHER" ("DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID") VALUES
                    (true, 'foo', 'org.hyades.notification.publisher.WebhookPublisher', 'template','text/plain', '1781db56-51a8-462a-858c-6030a2341dfc')
                RETURNING "ID";
                """).getSingleResult();
        entityManager.createNativeQuery("""            
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "NOTIFICATION_LEVEL", "SCOPE", "UUID", "PUBLISHER_CONFIG") VALUES
                    (true, 'foo', :publisherId, 'NEW_VULNERABILITY', false, 'INFORMATIONAL', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62', :publisherConfig);
                """).setParameter("publisherId", publisherId).setParameter("publisherConfig", publisherConfig).executeUpdate();
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('mattermost', 'general', 'STRING', 'base.url', 'http://localhost:1080/mychannel');
                """).executeUpdate();

        inputTopic.pipeInput(notification);

        verify(1, postRequestedFor(urlMatching("/instance-dsl")));
    }

}