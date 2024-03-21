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
package org.dependencytrack.repometaanalyzer.serde;

import com.github.packageurl.PackageURL;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaPurlSerde implements Serde<PackageURL> {

    private final Serializer<PackageURL> serializer = new KafkaPurlSerializer();
    private final Deserializer<PackageURL> deserializer = new KafkaPurlDeserializer();

    @Override
    public Serializer<PackageURL> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<PackageURL> deserializer() {
        return deserializer;
    }

}
