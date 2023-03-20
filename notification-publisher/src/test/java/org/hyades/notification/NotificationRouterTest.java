package org.hyades.notification;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.hyades.commonnotification.NotificationGroup;
import org.hyades.commonnotification.NotificationScope;
import org.hyades.model.Notification;
import org.hyades.model.NotificationLevel;
import org.hyades.notification.publisher.ConsolePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.math.BigInteger;

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
    void testConsolePublisher() {
        final var publisherId = (BigInteger) entityManager.createNativeQuery("""
                INSERT INTO "NOTIFICATIONPUBLISHER" ("DEFAULT_PUBLISHER", "NAME", "PUBLISHER_CLASS", "TEMPLATE", "TEMPLATE_MIME_TYPE", "UUID") VALUES
                    (true, 'foo', 'org.hyades.notification.publisher.ConsolePublisher', 'template','text/plain', '1781db56-51a8-462a-858c-6030a2341dfc')
                RETURNING "ID";
                """).getSingleResult();
        entityManager.createNativeQuery("""            
                INSERT INTO "NOTIFICATIONRULE" ("ENABLED", "NAME", "PUBLISHER", "NOTIFY_ON", "NOTIFY_CHILDREN", "NOTIFICATION_LEVEL", "SCOPE", "UUID") VALUES
                    (true, 'foo', :publisherId, 'NEW_VULNERABILITY', false, 'WARNING', 'PORTFOLIO', '6b1fee41-4178-4a23-9d1b-e9df79de8e62');
                """).setParameter("publisherId", publisherId).executeUpdate();

        final var notification = new Notification()
                .scope(NotificationScope.PORTFOLIO)
                .level(NotificationLevel.WARNING)
                .group(NotificationGroup.NEW_VULNERABILITY.name())
                .content("content");
        notificationRouter.inform(notification);

        Mockito.verify(consolePublisherMock).inform(Mockito.eq(notification), Mockito.any());
    }


}