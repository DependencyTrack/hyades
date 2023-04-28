package org.hyades.notification.publisher;


import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.hyades.proto.notification.v1.Notification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyades.proto.notification.v1.Group.GROUP_NEW_VULNERABILITY;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.hyades.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@QuarkusTest
public class JiraPublisherTest {

    @Inject
    JiraPublisher publisher;

    @Inject
    EntityManager entityManager;

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void beforeClass() {
        mockServer = startClientAndServer(1040);
    }

    @AfterAll
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    public void testPublish() throws Exception {
        final var jiraUser = "jiraUser";
        final var jiraPassword = "jiraPassword";

        final var request = request()
                .withMethod("POST")
                .withHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((jiraUser + ":" + jiraPassword).getBytes()));
        mockServer.when(request)
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                );
        final JsonObject config = getConfig(DefaultNotificationPublishers.JIRA, "MyProjectKey");

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();

        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('jira', 'general', 'URL', 'jira.url', 'http://localhost:1080');
                """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('jira', 'general', 'STRING', 'jira.username', 'test');
                """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('jira', 'general', 'ENCRYPTEDSTRING', 'jira.password', 'encr');
                """).executeUpdate();

        publisher.inform(notification, config);
        mockServer.verify(request);

    }

    JsonObject getConfig(DefaultNotificationPublishers publisher, String destination) throws IOException {
        String templateContent = IOUtils.resourceToString(publisher.getPublisherTemplateFile(), UTF_8);
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, publisher.getTemplateMimeType())
                .add(Publisher.CONFIG_TEMPLATE_KEY, templateContent)
                .add(Publisher.CONFIG_DESTINATION, destination)
                .addAll(getExtraConfig())
                .build();
    }

    public JsonObjectBuilder getExtraConfig() {
        return Json.createObjectBuilder()
                .add("jiraTicketType", "Task");
    }
}
