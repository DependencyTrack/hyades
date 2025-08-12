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
package org.dependencytrack.kstreams.statestore;

import org.apache.kafka.streams.state.internals.InMemoryKeyValueBytesStoreSupplier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StateStoreUtilTest {

    @Test
    void configurableKeyValueStoreWithoutQuarkusConfigTest() {
        assertThat(StateStoreUtil.configurableKeyValueStore("storeName"))
                .isInstanceOf(InMemoryKeyValueBytesStoreSupplier.class);
    }

    @Test
    void defaultChangelogTopicConfigTest() {
        assertThat(StateStoreUtil.defaultChangelogTopicConfig())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "cleanup.policy", "compact",
                        "segment.bytes", "67108864",
                        "max.compaction.lag.ms", "1"
                ));
    }

}