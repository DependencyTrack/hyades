package org.acme;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.commonnotification.NotificationGroup;
import org.acme.commonnotification.NotificationScope;
import org.acme.model.Notification;
import org.acme.model.NotificationLevel;
import org.acme.notification.NotificationRouter;
import org.acme.notification.publisher.WebhookPublisher;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
@QuarkusTest
public class WireMockNotificationPublisher {

    @Inject
    EntityManager entityManager;
    @Inject
    Topology topology;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Notification> inputTopic;
    private NotificationRouter routerMock;

    @BeforeEach
    void beforeEach() {
        routerMock = Mockito.mock(NotificationRouter.class);
        QuarkusMock.installMockForType(routerMock, NotificationRouter.class);

        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic("dtrack.notification.new_vulnerability", new StringSerializer(), new ObjectMapperSerializer<>());

    }


    @TestTransaction
    @Test
    public void testPublisher(WireMockRuntimeInfo wmRuntimeInfo){

        final var notification = new Notification()
                .scope(NotificationScope.PORTFOLIO)
                .level(NotificationLevel.WARNING)
                .group(NotificationGroup.NEW_VULNERABILITY.name())
                .content("content");

        stubFor(get("/static-dsl").willReturn(ok()));
        // Instance DSL can be obtained from the runtime info parameter
        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/instance-dsl").willReturn(ok()));
        System.out.print("mock url" +wmRuntimeInfo.getHttpsBaseUrl());
        String wireMockDestination = wmRuntimeInfo.getHttpsBaseUrl();

        String publisherConfig = "{\"destination\":\"" + wireMockDestination + "\"}";

        final int publisherId = (int) entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONPUBLISHER" ("DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID") VALUES
                    (true, 'foo', 'org.acme.notification.publisher.WebhookPublisher', 'template','text/plain', '1781db56-51a8-462a-858c-6030a2341dfc')
                RETURNING "ID";
                """).getSingleResult();
        entityManager.createNativeQuery("""            
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "NOTIFICATION_LEVEL", "SCOPE", "UUID", "PUBLISHER_CONFIG") VALUES
                    (true, 'foo', :publisherId, 'NEW_VULNERABILITY', false, 'WARNING', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62', :publisherConfig);
                """).setParameter("publisherId", publisherId).setParameter("publisherConfig", publisherConfig).executeUpdate();

        verify(1, postRequestedFor(urlEqualTo(wireMockDestination)));

    }

}