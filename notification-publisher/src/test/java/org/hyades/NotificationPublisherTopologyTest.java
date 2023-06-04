package org.hyades;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.hyades.notification.NotificationRouter;
import org.hyades.proto.KafkaProtobufSerializer;
import org.hyades.proto.notification.v1.Group;
import org.hyades.proto.notification.v1.Level;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.proto.notification.v1.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;

@QuarkusTest
public class NotificationPublisherTopologyTest {

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
        inputTopic = testDriver.createInputTopic("dtrack.notification.new_vulnerability", new StringSerializer(), new KafkaProtobufSerializer<>());

    }

    @Test
    void testNotificationTopology() throws Exception {
        final var notification = org.hyades.proto.notification.v1.Notification.newBuilder()
                .setScope(Scope.SCOPE_PORTFOLIO)
                .setLevel(Level.LEVEL_INFORMATIONAL)
                .setGroup(Group.GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();

        inputTopic.pipeInput(notification);

        Mockito.verify(routerMock).inform(Mockito.any());

    }
}
