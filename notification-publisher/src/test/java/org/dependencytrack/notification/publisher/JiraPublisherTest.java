package org.dependencytrack.notification.publisher;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.json.JsonObjectBuilder;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.persistence.model.ConfigPropertyConstants;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@QuarkusTest
public class JiraPublisherTest extends AbstractWebhookPublisherTest<JiraPublisher> {

    @Inject
    SecretDecryptor secretDecryptor;

    private Callable<Void> configPropertyCustomizer;

    @Override
    void setupConfigProperties() throws Exception {
        super.setupConfigProperties();

        createOrUpdateConfigProperty(ConfigPropertyConstants.JIRA_URL, wireMockServer.baseUrl());
        createOrUpdateConfigProperty(ConfigPropertyConstants.JIRA_USERNAME, "jiraUser");
        createOrUpdateConfigProperty(ConfigPropertyConstants.JIRA_PASSWORD, secretDecryptor.encryptAsString("jiraPassword"));

        if (configPropertyCustomizer != null) {
            configPropertyCustomizer.call();
        }
    }

    @Override
    JsonObjectBuilder extraConfig() {
        return super.extraConfig()
                .add(Publisher.CONFIG_DESTINATION, "PROJECT")
                .add("jiraTicketType", "Task");
    }

    @Override
    @TestTransaction
    void testInformWithBomConsumedNotification() throws Exception {
        super.testInformWithBomConsumedNotification();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_CONSUMED] Bill of Materials Consumed",
                            "description" : "A CycloneDX BOM was consumed and will be processed\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_INFORMATIONAL\\n\\n"
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotification() throws Exception {
        super.testInformWithBomProcessingFailedNotification();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_PROCESSING_FAILED] Bill of Materials Processing Failed",
                            "description" : "An error occurred while processing a BOM\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_ERROR\\n\\n"
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject() throws Exception {
        super.testInformWithBomProcessingFailedNotificationAndNoSpecVersionInSubject();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_PROCESSING_FAILED] Bill of Materials Processing Failed",
                            "description" : "An error occurred while processing a BOM\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_ERROR\\n\\n"
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithDataSourceMirroringNotification() throws Exception {
        super.testInformWithDataSourceMirroringNotification();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_DATASOURCE_MIRRORING] GitHub Advisory Mirroring",
                            "description" : "An error occurred mirroring the contents of GitHub Advisories. Check log for details.\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_ERROR\\n\\n"
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithNewVulnerabilityNotification() throws Exception {
        super.testInformWithNewVulnerabilityNotification();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_NEW_VULNERABILITY] [MEDIUM] New medium vulnerability identified: INT-001",
                            "description" : "A new vulnerability has been identified on your project(s).\\n\\\\\\\\\\n\\\\\\\\\\n*Vulnerability description*\\n{code:none|bgColor=white|borderStyle=none}vulnerabilityDescription{code}\\n\\n*VulnID*\\nINT-001\\n\\n*Severity*\\nMedium\\n\\n*Component*\\n[componentName : componentVersion|https://example.com/components/94f87321-a5d1-4c2f-b2fe-95165debebc6]\\n\\n*Affected project(s)*\\n- [projectName (projectVersion)|https://example.com/projects/c9c9539a-e381-4b36-ac52-6a7ab83b2c95]\\n"
                          }
                        }
                        """)));
    }

    @Override
    @TestTransaction
    void testInformWithProjectAuditChangeNotification() throws Exception {
        super.testInformWithProjectAuditChangeNotification();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Basic amlyYVVzZXI6amlyYVBhc3N3b3Jk"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_PROJECT_AUDIT_CHANGE] Analysis Decision: Finding Suppressed",
                            "description" : "\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_INFORMATIONAL\\n\\n"
                          }
                        }
                        """)));
    }

    @Test
    @TestTransaction
    void testInformWithBearerToken() throws Exception {
        configPropertyCustomizer = () -> {
            createOrUpdateConfigProperty(ConfigPropertyConstants.JIRA_USERNAME, null);
            createOrUpdateConfigProperty(ConfigPropertyConstants.JIRA_PASSWORD, secretDecryptor.encryptAsString("jiraToken"));
            return null;
        };

        super.testInformWithBomConsumedNotification();

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/rest/api/2/issue"))
                .withHeader("Authorization", equalTo("Bearer jiraToken"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "fields" : {
                            "project" : {
                              "key" : "PROJECT"
                            },
                            "issuetype" : {
                              "name" : "Task"
                            },
                            "summary" : "[Dependency-Track] [GROUP_BOM_CONSUMED] Bill of Materials Consumed",
                            "description" : "A CycloneDX BOM was consumed and will be processed\\n\\\\\\\\\\n\\\\\\\\\\n*Level*\\nLEVEL_INFORMATIONAL\\n\\n"
                          }
                        }
                        """)));
    }

}
