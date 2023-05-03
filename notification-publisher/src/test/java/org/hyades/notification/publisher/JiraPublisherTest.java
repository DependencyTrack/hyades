package org.hyades.notification.publisher;


import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.hyades.common.SecretDecryptor;
import org.hyades.proto.notification.v1.Notification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.HttpHeaders;
import java.util.Base64;
import java.util.Map;

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

    @Inject
    SecretDecryptor secretDecryptor;

    @BeforeAll
    public static void beforeClass() {
        mockServer = startClientAndServer(1080);
    }

    @AfterAll
    public static void afterClass() {
        mockServer.stop();
    }

    @Test
    @TestTransaction
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

        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('general', 'integrations', 'STRING', 'jira.url', 'http://localhost:1080');
                """).executeUpdate();

        entityManager.createNativeQuery("""
                INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                                    ('general', 'integrations', 'STRING', 'jira.username', 'jiraUser');
                """).executeUpdate();

        entityManager.createNativeQuery("""
                        INSERT INTO "CONFIGPROPERTY" ("DESCRIPTION", "GROUPNAME", "PROPERTYTYPE", "PROPERTYNAME", "PROPERTYVALUE") VALUES
                            ('general', 'integrations', 'ENCRYPTEDSTRING', 'jira.password', :encryptedPassword);
                        """)
                .setParameter("encryptedPassword", secretDecryptor.encryptAsString(jiraPassword))
                .executeUpdate();

        final var notification = Notification.newBuilder()
                .setScope(SCOPE_PORTFOLIO)
                .setGroup(GROUP_NEW_VULNERABILITY)
                .setLevel(LEVEL_INFORMATIONAL)
                .setTitle("Test Notification")
                .setContent("This is only a test")
                .build();

        final JsonObject config = getConfig("http://localhost:1080");
        publisher.inform(notification, config);
        mockServer.verify(request);
    }

    JsonObject getConfig(String destination) {
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, "testType")
                .add(Publisher.CONFIG_TEMPLATE_KEY, "templateContent")
                .add(Publisher.CONFIG_DESTINATION, destination)
                .addAll(Json.createObjectBuilder()
                        .add("jiraTicketType", "Task"))
                .build();
    }
}
