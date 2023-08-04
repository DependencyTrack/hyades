/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.hyades.repositories;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.hyades.model.MetaAnalyzerException;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.RepositoryType;

import java.io.IOException;

/**
 * Interface that defines Repository Meta Analyzers.
 *
 * @author Steve Springett
 * @since 3.1.0
 */
public interface IMetaAnalyzer {

    /**
     * Sets the {@link CloseableHttpClient} to be used by the analyzer.
     *
     * @param httpClient The {@link CloseableHttpClient} to use
     */
    void setHttpClient(final CloseableHttpClient httpClient);

    /**
     * Sets the base URL for the repository being used. If not specified, IMetaAnalyzer implementations
     * should fall back to a default value (if one is available).
     *
     * @param baseUrl the base URL to the repository
     * @since 3.1.0
     */
    void setRepositoryBaseUrl(String baseUrl);

    /**
     * Sets the username and password (or access token) to use for authentication with the repository. Should not be used for repositories that do not
     * use Basic authentication.
     *
     * @param username the username for access to the repository.
     * @param password the password or access token to be used for the repository.
     * @since 4.6.0
     */
    void setRepositoryUsernameAndPassword(String username, String password);

    /**
     * Returns the type of repositry the analyzer supports.
     *
     * @since 3.1.0
     */
    RepositoryType supportedRepositoryType();

    /**
     * Returns whether or not the analyzer is capable of supporting the ecosystem of the component.
     *
     * @param component the component to analyze
     * @return true if analyzer can be used for this component, false if not
     * @since 3.1.0
     */
    boolean isApplicable(Component component);

    /**
     * The component to analyze.
     *
     * @param component the component to analyze
     * @return a MetaModel object
     * @throws MetaAnalyzerException in case of any issue during metadata generation
     * @since 3.1.0
     */
    MetaModel analyze(Component component);

    String getName();

    CloseableHttpResponse getIntegrityCheckResponse(PackageURL packageURL) throws MalformedPackageURLException, IOException;

}
