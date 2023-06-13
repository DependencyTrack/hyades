package org.hyades.notification;

import org.hyades.notification.publisher.SlackPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PublisherClassTest {

    @Test
    public void getPublisherClassTest() throws ClassNotFoundException {

        Class<?> publisherClass = PublisherClass.getPublisherClass("org.dependencytrack.SlackPublisher");
        Assertions.assertEquals(SlackPublisher.class, publisherClass);

        publisherClass = PublisherClass.getPublisherClass("org.dependencytrack.InvalidPublisher");
        Assertions.assertNull(publisherClass);

        publisherClass = PublisherClass.getPublisherClass("SlackPublisher");
        Assertions.assertEquals(SlackPublisher.class, publisherClass);

        publisherClass = PublisherClass.getPublisherClass("p1.p2.SlackPublisher");
        Assertions.assertEquals(SlackPublisher.class, publisherClass);
    }
}
