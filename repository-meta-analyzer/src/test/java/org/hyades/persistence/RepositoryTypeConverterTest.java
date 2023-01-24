package org.hyades.persistence;

import org.hyades.model.RepositoryType;
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