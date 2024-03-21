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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.dependencytrack.persistence.model.IConfigProperty;

import static java.util.Optional.ofNullable;

@Converter(autoApply = true)
public class PropertyTypeConverter implements AttributeConverter<IConfigProperty.PropertyType, String> {

    @Override
    public String convertToDatabaseColumn(final IConfigProperty.PropertyType entityValue) {
        return ofNullable(entityValue)
                .map(propertyType -> propertyType.toString())
                .orElse(null);
    }

    @Override
    public IConfigProperty.PropertyType convertToEntityAttribute(final String databaseValue) {
        return ofNullable(databaseValue)
                .map(databasePropertyType -> IConfigProperty.PropertyType.valueOf(databasePropertyType))
                .orElse(null);
    }
}

