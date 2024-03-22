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
package org.dependencytrack.vulnmirror;

import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
class KafkaProducerProducer {

    private final Producer<String, byte[]> producer;

    KafkaProducerProducer(@Identifier("default-kafka-broker") final Map<String, Object> config) {
        this.producer = createProducer(config);
    }

    @Produces
    @ApplicationScoped
    Producer<String, byte[]> producer() {
        return producer;
    }

    void onStop(@Observes final ShutdownEvent event) {
        if (producer != null) {
            producer.close();
        }
    }

    private Producer<String, byte[]> createProducer(final Map<String, Object> config) {
        final var producerConfig = new HashMap<String, Object>();

        for (final Map.Entry<String, Object> entry : config.entrySet()) {
            if (ProducerConfig.configNames().contains(entry.getKey())) {
                producerConfig.put(entry.getKey(), entry.getValue());
            }
        }

        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return new KafkaProducer<>(producerConfig);
    }

}
