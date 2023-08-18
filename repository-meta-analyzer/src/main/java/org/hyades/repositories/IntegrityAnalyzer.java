package org.hyades.repositories;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hyades.persistence.model.RepositoryType;

import java.io.IOException;

/**
 * Interface that defines Integrity check analyzers.
 */
public interface IntegrityAnalyzer {

    RepositoryType supportedRepositoryType();

    String getName();

    default CloseableHttpResponse getIntegrityCheckResponse(String purl) throws MalformedPackageURLException {
        return null;
    }

    void setHttpClient(final CloseableHttpClient httpClient);

    void setRepositoryBaseUrl(String baseUrl);

    void setRepositoryUsernameAndPassword(String username, String password);
}
