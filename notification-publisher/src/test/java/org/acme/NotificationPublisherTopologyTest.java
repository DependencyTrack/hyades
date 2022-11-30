package org.acme;

import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.commonnotification.NotificationGroup;
import org.acme.commonnotification.NotificationScope;
import org.acme.model.Notification;
import org.acme.model.NotificationLevel;
import org.acme.notification.NotificationRouter;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javax.inject.Inject;

@QuarkusTest
public class NotificationPublisherTopologyTest {
    @Inject
    Topology topology;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Notification> inputTopic;
    private  NotificationRouter routerMock;


    @BeforeEach
    void beforeEach() {
        routerMock = Mockito.mock(NotificationRouter.class);
        QuarkusMock.installMockForType(routerMock, NotificationRouter.class);

        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic("dtrack.notification.new_vulnerability", new StringSerializer(), new ObjectMapperSerializer<>());

    }

    @Test
    void testNotificationTopology(){
        final var notification = new Notification()
                .scope(NotificationScope.PORTFOLIO)
                .level(NotificationLevel.WARNING)
                .group(NotificationGroup.NEW_VULNERABILITY.name())
                .content("content");

        inputTopic.pipeInput(notification);

        Mockito.verify(routerMock).inform(Mockito.any());

    }
}
