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
package org.dependencytrack.repometaanalyzer.processor;

import com.github.packageurl.PackageURL;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.dependencytrack.common.SecretDecryptor;
import org.dependencytrack.persistence.repository.RepoEntityRepository;
import org.dependencytrack.proto.repometaanalysis.v1.AnalysisCommand;
import org.dependencytrack.proto.repometaanalysis.v1.AnalysisResult;
import org.dependencytrack.repometaanalyzer.repositories.RepositoryAnalyzerFactory;

@ApplicationScoped
public class MetaAnalyzerProcessorSupplier implements FixedKeyProcessorSupplier<PackageURL, AnalysisCommand, AnalysisResult> {

    private final RepoEntityRepository repoEntityRepository;
    private final RepositoryAnalyzerFactory analyzerFactory;
    private final SecretDecryptor secretDecryptor;
    private final Cache cache;

    public MetaAnalyzerProcessorSupplier(final RepoEntityRepository repoEntityRepository,
                                         final RepositoryAnalyzerFactory analyzerFactory,
                                         final SecretDecryptor secretDecryptor,
                                         @CacheName("metaAnalyzer") final Cache cache) {
        this.repoEntityRepository = repoEntityRepository;
        this.analyzerFactory = analyzerFactory;
        this.secretDecryptor = secretDecryptor;
        this.cache = cache;
    }

    @Override
    public FixedKeyProcessor<PackageURL, AnalysisCommand, AnalysisResult> get() {
        return new MetaAnalyzerProcessor(repoEntityRepository, analyzerFactory, secretDecryptor, cache);
    }

}
