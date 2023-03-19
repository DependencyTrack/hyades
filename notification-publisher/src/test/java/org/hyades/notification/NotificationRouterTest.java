package org.hyades.notification;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.hyades.notification.publisher.ConsolePublisher;
import org.hyades.proto.notification.v1.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.math.BigInteger;

import static org.hyades.proto.notification.v1.Group.GROUP_NEW_VULNERABILITY;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.hyades.proto.notification.v1.Scope.SCOPE_PORTFOLIO;

@QuarkusTest
class NotificationRouterTest {

    @Inject
    EntityManager entityManager;

    @Inject
    NotificationRouter notificationRouter;

    private ConsolePublisher consolePublisherMock;

    @BeforeEach
    void setUp() {
        consolePublisherMock = Mockito.mock(ConsolePublisher.class);
        QuarkusMock.installMockForType(consolePublisherMock, ConsolePublisher.class);
    }


    @Test
    @TestTransaction
    void testConsolePublisher() throws Exception {
        final var publisherId = (BigInteger) entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONPUBLISHER" ("DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID") VALUES
                    (true, 'foo', 'org.hyades.notification.publisher.ConsolePublisher', 'template','text/plain', '1781db56-51a8-462a-858c-6030a2341dfc')
                RETURNING "ID";
                """).getSingleResult();
        entityManager.createNativeQuery("""            
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "NOTIFICATION_LEVEL", "SCOPE", "UUID") VALUES
                    (true, 'foo', :publisherId, 'NEW_VULNERABILITY', false, 'INFORMATIONAL', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62');
                """).setParameter("publisherId", publisherId).executeUpdate();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setLevel(LEVEL_INFORMATIONAL)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();
        notificationRouter.inform(notification);

        Mockito.verify(consolePublisherMock).inform(Mockito.eq(notification), Mockito.any());
    }


}