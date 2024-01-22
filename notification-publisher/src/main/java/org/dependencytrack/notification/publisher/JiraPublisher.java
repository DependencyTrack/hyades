package org.dependencytrack.notification.publisher;

import io.pebbletemplates.pebble.PebbleEngine;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.persistence.repository.ConfigPropertyRepository;
import org.dependencytrack.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.dependencytrack.persistence.model.ConfigPropertyConstants.JIRA_PASSWORD;
import static org.dependencytrack.persistence.model.ConfigPropertyConstants.JIRA_USERNAME;

@ApplicationScoped
@Startup // Force bean creation even though no direct injection points exist
public class JiraPublisher extends AbstractWebhookPublisher implements Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraPublisher.class);
    private final ConfigPropertyRepository configPropertyRepository;
    private final SecretDecryptor secretDecryptor;
    private static final PebbleEngine ENGINE = new PebbleEngine.Builder().defaultEscapingStrategy("json").build();
    private String jiraProjectKey;
    private String jiraTicketType;

    public JiraPublisher(ConfigPropertyRepository configPropertyRepository, SecretDecryptor secretDecryptor) {
        this.configPropertyRepository = configPropertyRepository;
        this.secretDecryptor = secretDecryptor;
    }

    @Override
    public String getDestinationUrl(final JsonObject config) {
        final String baseUrl = config.getString(CONFIG_DESTINATION);
        return (baseUrl.endsWith("/") ? baseUrl : baseUrl + '/') + "rest/api/2/issue";
    }

    @Override
    protected BasicAuthCredentials getBasicAuthCredentials() throws Exception {
            final String jiraUsername = configPropertyRepository.findByGroupAndName(JIRA_USERNAME.getGroupName(), JIRA_USERNAME.getPropertyName()).getPropertyValue();
            final String encryptedPassword = configPropertyRepository.findByGroupAndName(JIRA_PASSWORD.getGroupName(), JIRA_PASSWORD.getPropertyName()).getPropertyValue();
            final String jiraPassword = (encryptedPassword == null) ? null : secretDecryptor.decryptAsString(encryptedPassword);
            return new BasicAuthCredentials(jiraUsername, jiraPassword);
    }

    @Override
    public void inform(final PublishContext ctx, final Notification notification, final JsonObject config) throws Exception {
        if (config == null) {
            LOGGER.warn("No publisher configuration provided; Skipping notification (%s)".formatted(ctx));
            return;
        }

        jiraTicketType = config.getString("jiraTicketType", null);
        if (jiraTicketType == null) {
            LOGGER.warn("No JIRA ticket type configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        jiraProjectKey = config.getString(CONFIG_DESTINATION, null);
        if (jiraProjectKey == null) {
            LOGGER.warn("No JIRA project key configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        publish(ctx, getTemplate(config), notification, config, configPropertyRepository);
    }

    @Override
    public PebbleEngine getTemplateEngine() {
        return ENGINE;
    }

    @Override
    public void enrichTemplateContext(final Map<String, Object> context, JsonObject config) {
        jiraTicketType = config.getString("jiraTicketType");
        jiraProjectKey = config.getString(CONFIG_DESTINATION);
        context.put("jiraProjectKey", jiraProjectKey);
        context.put("jiraTicketType", jiraTicketType);
    }
}
