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
package org.dependencytrack.vulnmirror.datasource.csaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.csaf.sbom.retrieval.CsafLoader;
import io.ktor.client.engine.mock.MockEngine;
import io.ktor.http.HttpStatusCode;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.persistence.model.CsafSourceEntity;
import org.dependencytrack.persistence.repository.CsafSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.ktor.client.engine.mock.MockUtilsKt.respond;
import static io.ktor.http.HeadersKt.headersOf;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.Mockito.when;

@QuarkusTest
    @TestProfile(CsafMirrorTest.TestProfile.class)
    @QuarkusTestResource(KafkaCompanionResource.class)
    class CsafMirrorTest {

        public static final class TestProfile implements QuarkusTestProfile {

            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.ofEntries(
                        Map.entry("dtrack.vuln-source.csaf.enabled", "true")
                );
            }

        }

        @InjectMock
        CsafLoaderFactory csafLoaderFactory;

        @Inject
        @Default
        ObjectMapper objectMapper;

        @Inject
        EntityManager entityManager;

        @Inject
        CsafSourceRepository sourceRepository;

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

    @Inject
    CsafMirror csafMirror;

        @AfterEach
        void afterEach() {
            // Publish tombstones to the vulnerability digest topic for all vulnerabilities used in this test.
            kafkaCompanion.produce(Serdes.String(), Serdes.ByteArray())
                    .fromRecords(List.of(
                            new ProducerRecord<>(KafkaTopic.VULNERABILITY_DIGEST.getName(), "CSAF/CSAF-12345678-TRACKINGID-VULNERABILITY0", null)
                    ))
                    .awaitCompletion();
        }

        CsafLoader createLoader() {
            var engine = MockEngine.Companion.invoke((scope, request, cont) -> {
                cont.resumeWith(respond(scope, "Not Found", new HttpStatusCode(400, "Not found"), headersOf()));

                return null;
            });
            return new CsafLoader(engine);
        }


    @BeforeEach
    @Transactional
    public void insertTestSource() {
        var csafSource = new CsafSourceEntity();
        csafSource.setName("Test CSAF Source");
        csafSource.setUrl("https://example.com/csaf");
        csafSource.setEnabled(true);
        csafSource.setAggregator(false);
        sourceRepository.persistAndFlush(csafSource);
    }


    @Test
    @TestTransaction
    void testDoMirrorSuccessNotification() {
            /*var csafSource = new CsafSourceEntity();
            csafSource.setName("Test CSAF Source");
            csafSource.setUrl("https://example.com/csaf");
            csafSource.setEnabled(true);
            csafSource.setAggregator(false);

            entityManager.persist(csafSource);
            entityManager.flush();*/
        // Works only within the test but not in the called mirror-function :(
        //sourceRepository.persistAndFlush(csafSource);
        var test = sourceRepository.findAll().list();

            when(csafLoaderFactory.create()).thenReturn(createLoader());

        assertThatNoException().isThrownBy(() -> csafMirror.doMirror().get());
    }
}
