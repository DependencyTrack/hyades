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

import org.dependencytrack.persistence.model.IConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertyTypeConverterTest {

    @Test
    public void convertToDatastoreTest() {
        Assertions.assertNull((new PropertyTypeConverter().convertToDatabaseColumn(null)));
        Assertions.assertTrue((new PropertyTypeConverter().convertToDatabaseColumn(IConfigProperty.PropertyType.ENCRYPTEDSTRING).equals("ENCRYPTEDSTRING")));
        Assertions.assertTrue((new PropertyTypeConverter().convertToDatabaseColumn(IConfigProperty.PropertyType.BOOLEAN).equals("BOOLEAN")));
    }

    @Test
    public void convertToAttributeTest() {
        Assertions.assertEquals(new PropertyTypeConverter().convertToEntityAttribute("ENCRYPTEDSTRING"), IConfigProperty.PropertyType.ENCRYPTEDSTRING);
        Assertions.assertEquals(new PropertyTypeConverter().convertToEntityAttribute("BOOLEAN"), IConfigProperty.PropertyType.BOOLEAN);

    }

}
