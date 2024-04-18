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
package org.dependencytrack.persistence.mapping;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public abstract class MultiValueMapRowReducer<K, V> implements RowReducer<Map<K, Set<V>>, Map.Entry<K, Set<V>>> {

    @Override
    public Map<K, Set<V>> container() {
        return new HashMap<>();
    }

    @Override
    public void accumulate(final Map<K, Set<V>> container, final RowView rowView) {
        container.compute(extractKey(rowView), (key, values) -> mapValues(rowView, key, values));
    }

    @Override
    public Stream<Map.Entry<K, Set<V>>> stream(final Map<K, Set<V>> container) {
        return container.entrySet().stream();
    }

    protected abstract K extractKey(final RowView rowView);

    protected abstract Set<V> mapValues(final RowView rowView, final K key, final Set<V> values);

}
