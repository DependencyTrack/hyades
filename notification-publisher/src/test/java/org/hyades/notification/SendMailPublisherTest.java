package org.hyades.notification;

import io.quarkus.test.junit.QuarkusTest;
import org.hyades.notification.publisher.SendMailPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;

@QuarkusTest
public class SendMailPublisherTest {

    @Test
    public void testSingleDestination() {
        JsonObject config = configWithDestination("john@doe.com");
        Assertions.assertArrayEquals(new String[]{"john@doe.com"}, SendMailPublisher.parseDestination(config));
    }


    @Test
    public void testMultipleDestinations() {
        JsonObject config = configWithDestination("john@doe.com,steve@jobs.org");
        Assertions.assertArrayEquals(new String[]{"john@doe.com", "steve@jobs.org"},
                SendMailPublisher.parseDestination(config));
    }


    @Test
    public void testEmptyDestinations() {
        JsonObject config = configWithDestination("");
        Assertions.assertArrayEquals(null, SendMailPublisher.parseDestination(config));
    }

    private static JsonObject configWithDestination(final String destination) {
        return Json.createObjectBuilder().add("destination", destination).build();
    }

}
