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
package org.dependencytrack.persistence.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CollectionIntegerConverterTest {

    @Test
    public void convertToDatastoreTest() {
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(null) == null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(List.of()).isEmpty());
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(List.of(666)).equals("666"));
        Assertions.assertTrue(new CollectionIntegerConverter().convertToDatabaseColumn(List.of(666, 123)).equals("666,123"));
    }

    @Test
    public void convertToAttributeTest() {
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute(null) == null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute("")== null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute(" ")== null);
        Assertions.assertTrue(new CollectionIntegerConverter().convertToEntityAttribute("666").contains(666));
    }

}