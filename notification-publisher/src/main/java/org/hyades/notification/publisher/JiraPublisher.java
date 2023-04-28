package org.hyades.notification.publisher;

import io.pebbletemplates.pebble.PebbleEngine;
import io.quarkus.runtime.Startup;
import org.hyades.common.SecretDecryptor;
import org.hyades.persistence.ConfigPropertyRepository;
import org.hyades.proto.notification.v1.Notification;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;
import java.util.Map;

import static org.hyades.model.ConfigPropertyConstants.JIRA_PASSWORD;
import static org.hyades.model.ConfigPropertyConstants.JIRA_URL;
import static org.hyades.model.ConfigPropertyConstants.JIRA_USERNAME;

@ApplicationScoped
@Startup // Force bean creation even though no direct injection points exist
public class JiraPublisher extends AbstractWebhookPublisher implements Publisher {

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
        final String baseUrl = configPropertyRepository.findByGroupAndName(JIRA_URL.getGroupName(), JIRA_URL.getPropertyName()).getPropertyValue();
            return (baseUrl.endsWith("/") ? baseUrl : baseUrl + '/') + "rest/api/2/issue";
    }

    @Override
    public BasicAuthCredentials getBasicAuthCredentials() throws Exception {
            final String jiraUsername = configPropertyRepository.findByGroupAndName(JIRA_USERNAME.getGroupName(), JIRA_USERNAME.getPropertyName()).getPropertyValue();
            final String encryptedPassword = configPropertyRepository.findByGroupAndName(JIRA_PASSWORD.getGroupName(), JIRA_PASSWORD.getPropertyName()).getPropertyValue();
            final String jiraPassword = (encryptedPassword == null) ? null : secretDecryptor.decryptAsString(encryptedPassword);
            return new BasicAuthCredentials(jiraUsername, jiraPassword);
    }

    @Override
    public void inform(final Notification notification, final JsonObject config) throws Exception {
        publish(DefaultNotificationPublishers.JIRA.getPublisherName(), getTemplate(config), notification, config, configPropertyRepository);
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
