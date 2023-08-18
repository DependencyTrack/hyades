package org.hyades.repositories;

import org.apache.http.impl.client.CloseableHttpClient;
import org.hyades.model.IntegrityModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;

/**
 * Interface that defines Integrity check analyzers.
 */
public interface IntegrityAnalyzer {

    RepositoryType supportedRepositoryType();

    String getName();

    default IntegrityModel getIntegrityModel(Component component) {
        return null;
    }

    void setHttpClient(final CloseableHttpClient httpClient);

    void setRepositoryBaseUrl(String baseUrl);

    void setRepositoryUsernameAndPassword(String username, String password);
}
