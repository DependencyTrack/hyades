package org.dependencytrack.notification.publisher;


import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.HttpHeaders;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.proto.notification.v1.Notification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import java.util.Base64;
import java.util.Map;

import static org.dependencytrack.notification.publisher.PublisherTestUtil.createPublisherContext;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_NEW_VULNERABILITY;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_PORTFOLIO;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@QuarkusTest
@TestProfile(JiraPublisherTest.TestProfile.class)
public class JiraPublisherTest {

    public static class TestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.kafka.snappy.enabled", "false",
                    "client.http.config.proxy-timeout-connection", "20",
                    "client.http.config.proxy-timeout-pool", "40",
                    "client.http.config.proxy-timeout-socket", "20"
            );
        }
    }

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
        publisher.inform(createPublisherContext(notification), notification, config);
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
