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

import org.dependencytrack.persistence.model.RepositoryType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

class RepositoryTypeConverterTest {

    @ParameterizedTest
    @EnumSource(RepositoryType.class)
    void testConvertToDatabaseColumn(final RepositoryType repositoryType) {
        final var converter = new RepositoryTypeConverter();
        Assertions.assertEquals(repositoryType.name(), converter.convertToDatabaseColumn(repositoryType));
    }

    @ParameterizedTest
    @MethodSource("provideConvertToEntityAttributeArguments")
    void testConvertToEntityAttribute(final String columnValue, final RepositoryType expectedValue) {
        final var converter = new RepositoryTypeConverter();
        Assertions.assertEquals(expectedValue, converter.convertToEntityAttribute(columnValue));
    }

    private static Stream<Arguments> provideConvertToEntityAttributeArguments() {
        return Stream.concat(
                Stream.of(Arguments.of(null, null)),
                Arrays.stream(RepositoryType.values())
                        .map(value -> Arguments.of(value.name(), value)));
    }

}