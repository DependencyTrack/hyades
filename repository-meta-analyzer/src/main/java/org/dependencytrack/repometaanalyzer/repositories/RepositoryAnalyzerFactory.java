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
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.repometaanalyzer.repositories;

import com.github.packageurl.PackageURL;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@ApplicationScoped
public class RepositoryAnalyzerFactory {

    private static final Map<String, Supplier<IMetaAnalyzer>> ANALYZER_SUPPLIERS = Map.of(
            PackageURL.StandardTypes.COMPOSER, ComposerMetaAnalyzer::new,
            PackageURL.StandardTypes.GEM, GemMetaAnalyzer::new,
            PackageURL.StandardTypes.GOLANG, GoModulesMetaAnalyzer::new,
            PackageURL.StandardTypes.HEX, HexMetaAnalyzer::new,
            PackageURL.StandardTypes.MAVEN, MavenMetaAnalyzer::new,
            PackageURL.StandardTypes.NPM, NpmMetaAnalyzer::new,
            PackageURL.StandardTypes.NUGET, NugetMetaAnalyzer::new,
            PackageURL.StandardTypes.PYPI, PypiMetaAnalyzer::new,
            PackageURL.StandardTypes.CARGO, CargoMetaAnalyzer::new,
            "cpan", CpanMetaAnalyzer::new
    );

    private final CloseableHttpClient httpClient;

    RepositoryAnalyzerFactory(@Named("httpClient") final CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public boolean hasApplicableAnalyzer(final PackageURL purl) {
        return ANALYZER_SUPPLIERS.containsKey(purl.getType());
    }

    public Optional<IMetaAnalyzer> createAnalyzer(final PackageURL purl) {
        final Supplier<IMetaAnalyzer> analyzerSupplier = ANALYZER_SUPPLIERS.get(purl.getType());
        if (analyzerSupplier == null) {
            return Optional.empty();
        }


        final IMetaAnalyzer analyzer = analyzerSupplier.get();
        analyzer.setHttpClient(httpClient);
        return Optional.of(analyzer);
    }

}
