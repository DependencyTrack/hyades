package org.acme.notification;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.commonnotification.NotificationGroup;
import org.acme.commonnotification.NotificationScope;
import org.acme.model.Notification;
import org.acme.model.NotificationLevel;
import org.acme.notification.publisher.DefaultNotificationPublishers;
import org.acme.notification.publisher.MattermostPublisher;
import org.acme.notification.publisher.Publisher;
import org.acme.persistence.ConfigPropertyRepository;
import org.acme.util.NotificationUtil;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@QuarkusTest
public class MattermostPublisherTest {

    private static ClientAndServer mockServer;

    @Inject
    ConfigPropertyRepository configPropertyRepository;
    @Inject
    EntityManager entityManager;

    @BeforeEach
    public void beforeClass() {
        mockServer = startClientAndServer(1090);
    }

    @AfterEach
    public void afterClass() {
        mockServer.stop();
    }

    @Test
    @TestTransaction
    public void testPublish() throws IOException {
        new MockServerClient("localhost", 1090)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/mychannel")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                );

        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("ID", "DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    (1, 'mattermost', 'general', 'STRING', 'base.url', 'http://localhost:1090/mychannel');
                """).executeUpdate();

        JsonObject config = getConfig(DefaultNotificationPublishers.MATTERMOST, "http://localhost:1090/mychannel");
        Notification notification = new Notification();
        notification.setScope(NotificationScope.PORTFOLIO.name());
        notification.setGroup(NotificationGroup.NEW_VULNERABILITY.name());
        notification.setLevel(NotificationLevel.INFORMATIONAL);
        notification.setTitle("Test Notification");
        notification.setContent("This is only a test");
        MattermostPublisher publisher = new MattermostPublisher(configPropertyRepository);
        publisher.inform(notification, config);
    }

    JsonObject getConfig(DefaultNotificationPublishers publisher, String destination) throws IOException {
        File templateFile = new File(URLDecoder.decode(NotificationUtil.class.getResource(publisher.getPublisherTemplateFile()).getFile(), UTF_8.name()));
        String templateContent = FileUtils.readFileToString(templateFile, UTF_8);
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, publisher.getTemplateMimeType())
                .add(Publisher.CONFIG_TEMPLATE_KEY, templateContent)
                .add(Publisher.CONFIG_DESTINATION, destination)
                .addAll(getExtraConfig())
                .build();
    }

    JsonObjectBuilder getExtraConfig() {
        return Json.createObjectBuilder();
    }
}
