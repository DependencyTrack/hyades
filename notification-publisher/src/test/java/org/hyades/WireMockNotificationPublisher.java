package org.hyades;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.hyades.common.KafkaTopic;
import org.hyades.commonnotification.NotificationGroup;
import org.hyades.commonnotification.NotificationScope;
import org.hyades.model.Notification;
import org.hyades.model.NotificationLevel;
import org.hyades.notification.NotificationRouter;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@QuarkusTest
public class WireMockNotificationPublisher {

    private static WireMockServer wireMockServer;
    @Inject
    EntityManager entityManager;
    @Inject
    Topology topology;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Notification> inputTopic;
    private NotificationRouter routerMock;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
    }

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.NOTIFICATION_NEW_VULNERABILITY.getName(),
                new StringSerializer(), new ObjectMapperSerializer<>());
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


    @TestTransaction
    @Test
    public void testPublisher(){

        final var notification = new Notification()
                .scope(NotificationScope.PORTFOLIO)
                .level(NotificationLevel.WARNING)
                .group(NotificationGroup.NEW_VULNERABILITY.name())
                .content("content");


        WireMock.stubFor(get("/instance-dsl").willReturn(ok()));
        String publisherConfig = "{\"destination\":\"" + wireMockServer.url("/instance-dsl") + "\"}";

        final int publisherId = (int) entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONPUBLISHER" ("DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID") VALUES
                    (true, 'foo', 'org.hyades.notification.publisher.WebhookPublisher', 'template','text/plain', '1781db56-51a8-462a-858c-6030a2341dfc')
                RETURNING "ID";
                """).getSingleResult();
        entityManager.createNativeQuery("""            
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "NOTIFICATION_LEVEL", "SCOPE", "UUID", "PUBLISHER_CONFIG") VALUES
                    (true, 'foo', :publisherId, 'NEW_VULNERABILITY', false, 'WARNING', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62', :publisherConfig);
                """).setParameter("publisherId", publisherId).setParameter("publisherConfig", publisherConfig).executeUpdate();
        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("ID", "DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    (1, 'mattermost', 'general', 'STRING', 'base.url', 'http://localhost:1080/mychannel');
                """).executeUpdate();

        inputTopic.pipeInput(notification);
        System.out.print(wireMockServer.baseUrl());
        verify(1, postRequestedFor(urlMatching("/instance-dsl")));

    }

}