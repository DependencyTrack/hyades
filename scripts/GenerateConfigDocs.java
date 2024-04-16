///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//DEPS info.picocli:picocli:4.7.5
//DEPS io.pebbletemplates:pebble:3.2.2

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

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command(name = "GenerateConfigDocs")
public class GenerateConfigDocs implements Callable<Integer> {

    @Parameters(
            index = "0",
            paramLabel = "PROPERTIES_FILE",
            description = "The properties file to generate documentation for"
    )
    private File propertiesFile;

    @Option(
            names = {"-t", "--template"},
            paramLabel = "TEMPLATE_FILE",
            description = "The Pebble template file to use for generation",
            required = true
    )
    private File templateFile;

    @Option(
            names = {"-o", "--output"},
            paramLabel = "OUTPUT_PATH",
            description = "Path to write the output to, will write to STDOUT if not provided"
    )
    private File outputFile;

    @Option(
            names = "--include-hidden",
            description = "Include hidden properties in the output"
    )
    private boolean includeHidden;

    private final PebbleEngine pebbleEngine = new PebbleEngine.Builder()
            .newLineTrimming(false)
            .autoEscaping(false)
            .build();
    private final String[] rawArgs;

    private GenerateConfigDocs(final String[] rawArgs) {
        this.rawArgs = rawArgs;
    }

    public static void main(final String[] args) {
        final var commandLine = new CommandLine(new GenerateConfigDocs(args));
        System.exit(commandLine.execute(args));
    }

    @Override
    public Integer call() throws Exception {
        final List<ConfigProperty> properties = collectProperties(propertiesFile, includeHidden);

        final Map<String, String> anchorsByPropertyName = properties.stream()
                .collect(Collectors.toMap(
                        property -> property.name,
                        ConfigProperty::anchor
                ));
        for (final ConfigProperty property : properties) {
            try {
                property.validate();
            } catch (RuntimeException e) {
                System.err.println(Ansi.AUTO.string("""
                        @|yellow [!] Definition of property %s appears to be invalid: %s|@\
                        """.formatted(property.name, e.getMessage())));
            }

            // Explicitly set the default to null when none is set already.
            if (property.defaultValue == null || property.defaultValue.isBlank()) {
                property.defaultValue = "null";
            }

            // Ensure enum properties also define valid values.
            if (property.type == PropertyType.ENUM
                && (property.validValues == null || property.validValues.isBlank())) {
                System.err.println(Ansi.AUTO.string("""
                        @|yellow [!] Property %s is of type enum, but does not define any valid values |@\
                        """.formatted(property.name)));
            }

            // Resolve known property names in the description and create hyperlinks for them.
            for (final Map.Entry<String, String> anchorByPropertyName : anchorsByPropertyName.entrySet()) {
                if (property.description == null || property.description.isBlank()) {
                    continue;
                }

                property.description = property.description.replaceAll(
                        Pattern.quote(anchorByPropertyName.getKey()),
                        "[`%s`](%s)".formatted(anchorByPropertyName.getKey(), anchorByPropertyName.getValue())
                );
            }
        }

        // Group properties by category.
        final Map<String, List<ConfigProperty>> propertiesByCategory = properties.stream()
                .collect(Collectors.groupingBy(
                        property -> property.category != null ? property.category : "Other",
                        Collectors.toList()
                ));

        // Sort categories and the properties within them by name to ensure stable output.
        final var propertiesByCategorySorted = new TreeMap<>(propertiesByCategory);
        propertiesByCategorySorted.replaceAll((category, propertiesList) ->
                propertiesList.stream().sorted(Comparator.comparing(property -> property.name)).toList());

        final PebbleTemplate template = pebbleEngine.getTemplate(templateFile.getAbsolutePath());
        final Map<String, Object> templateContext = Map.ofEntries(
                Map.entry("generateCommand", String.join(" ", rawArgs)),
                Map.entry("generateTimestamp", ZonedDateTime.now()),
                Map.entry("propertiesByCategory", propertiesByCategorySorted)
        );

        final var writer = new StringWriter();
        template.evaluate(writer, templateContext);

        if (outputFile == null) {
            System.out.println(writer);
            return 0;
        }

        try (final OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            outputStream.write(writer.toString().getBytes(StandardCharsets.UTF_8));
        }

        return 0;
    }

    public enum PropertyType {

        BOOLEAN,
        CRON,
        DOUBLE,
        DURATION,
        ENUM,
        INTEGER,
        STRING;


        @Override
        public String toString() {
            return name().toLowerCase();
        }

    }

    public static class ConfigProperty {

        public String name;
        public String defaultValue;
        public PropertyType type;
        public String validValues;
        public String description;
        public String example;
        public String category;
        public boolean required;
        public boolean hidden;

        public String env() {
            return name
                    .replaceAll("\\.", "_")
                    .replaceAll("-", "_")
                    .replaceAll("\"", "_")
                    .toUpperCase();
        }

        private String anchor() {
            return "#%s".formatted(name.replaceAll("\\.", "").toLowerCase());
        }

        private void validate() {
            // If a default value is provided, verify that it matches the intended type.
            if (defaultValue != null && !defaultValue.isEmpty()
                && !defaultValue.matches("^\\$\\{[\\w.]+}$")) {
                if (type == PropertyType.BOOLEAN && !defaultValue.matches("^(true|false)$")) {
                    throw new IllegalArgumentException("%s is not a valid boolean value".formatted(defaultValue));
                } else if (type == PropertyType.DOUBLE) {
                    var ignored = Double.parseDouble(defaultValue);
                } else if (type == PropertyType.DURATION) {
                    var ignored = Duration.parse(defaultValue);
                } else if (type == PropertyType.INTEGER) {
                    var ignored = Integer.parseInt(defaultValue);
                }
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            ConfigProperty that = (ConfigProperty) object;
            return required == that.required && hidden == that.hidden && Objects.equals(name, that.name) && Objects.equals(defaultValue, that.defaultValue) && type == that.type && Objects.equals(validValues, that.validValues) && Objects.equals(description, that.description) && Objects.equals(example, that.example) && Objects.equals(category, that.category);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, defaultValue, type, validValues, description, example, category, required, hidden);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ConfigProperty.class.getSimpleName() + "[", "]")
                    .add("name='" + name + "'")
                    .add("defaultValue='" + defaultValue + "'")
                    .add("type=" + type)
                    .add("validValues='" + validValues + "'")
                    .add("description='" + description + "'")
                    .add("example='" + example + "'")
                    .add("category='" + category + "'")
                    .add("required=" + required)
                    .add("hidden=" + hidden)
                    .toString();
        }

    }

    private static List<ConfigProperty> collectProperties(final File propertiesFile, final boolean includeHidden) throws IOException {
        List<String> lines = Files.readAllLines(propertiesFile.toPath());

        var currentProperty = new ConfigProperty();
        final var properties = new ArrayList<ConfigProperty>();

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex).strip();
            if (line.isEmpty()) {
                if (!Objects.equals(currentProperty, new ConfigProperty())) {
                    System.err.println(Ansi.AUTO.string("""
                            @|yellow [!] Detected empty line, discarding incomplete property: %s|@\
                            """.formatted(currentProperty)));
                    currentProperty = new ConfigProperty();
                }

                continue;
            }

            if (line.matches("^#[\\s\\w].*")) {
                line = line.substring(1).trim();
                if (line.matches("^@type:\\s.*")) {
                    final String[] parts = line.split(":", 2);
                    currentProperty.type = PropertyType.valueOf(parts[1].trim().toUpperCase());
                } else if (line.matches("^@category:\\s.*")) {
                    final String[] parts = line.split(":", 2);
                    currentProperty.category = parts[1].trim();
                } else if (line.matches("^@default:\\s.*")) {
                    final String[] parts = line.split(":", 2);
                    currentProperty.defaultValue = parts[1].trim();
                } else if (line.matches("^@example:\\s.*")) {
                    final String[] parts = line.split(":", 2);
                    currentProperty.example = parts[1].trim();
                } else if (line.matches("^@valid-values:\\s.*")) {
                    final String[] parts = line.split(":", 2);
                    currentProperty.validValues = parts[1].trim();
                } else if (line.matches("^@hidden\\s*$")) {
                    currentProperty.hidden = true;
                } else if (line.matches("^@required\\s*$")) {
                    currentProperty.required = true;
                } else if (line.matches("^[\\w.-]+=.*$")) {
                    final String[] parts = line.split("=", 2);
                    currentProperty.name = parts[0].trim();
                    String defaultValue = parts[1].trim();

                    // Deal with multi-line default values, indicated by a trailing backslash.
                    if (defaultValue.endsWith("\\")) {
                        defaultValue = defaultValue.replaceAll("\\\\$", "");

                        int nextLineIndex = lineIndex + 1;
                        while (nextLineIndex < lines.size() && lines.get(nextLineIndex).matches("^#\\s+.*")) {
                            defaultValue += lines.get(nextLineIndex).replaceFirst("^#", "").trim();
                            if (!defaultValue.endsWith("\\")) {
                                break;
                            }

                            defaultValue = defaultValue.replaceAll("\\\\$", "");
                            nextLineIndex++;
                        }

                        lineIndex = nextLineIndex;
                    }

                    if (currentProperty.name.startsWith("%")) {
                        System.err.println(Ansi.AUTO.string("""
                            @|yellow [!] Skipping profile-specific property %s|@\
                            """.formatted(currentProperty.name)));
                        currentProperty = new ConfigProperty();
                        continue;
                    }

                    if (currentProperty.defaultValue == null || currentProperty.defaultValue.isBlank()) {
                        currentProperty.defaultValue = defaultValue;
                    } else if (!defaultValue.isEmpty()) {
                        System.err.println(Ansi.AUTO.string("""
                            @|yellow [!] %s has both a default value (%s) and a @default annotation (%s)|@\
                            """.formatted(currentProperty.name, defaultValue, currentProperty.defaultValue)));
                    }

                    if (!currentProperty.hidden || includeHidden) {
                        properties.add(currentProperty);
                    }

                    currentProperty = new ConfigProperty();
                } else {
                    if (currentProperty.description == null) {
                        currentProperty.description = line + "  ";
                    } else {
                        currentProperty.description += line + "  ";
                    }
                }
            } else if (line.contains("=")) {
                final String[] parts = line.split("=", 2);
                currentProperty.name = parts[0].trim();
                String defaultValue = parts[1].trim();

                // Deal with multi-line default values, indicated by a trailing backslash.
                if (defaultValue.endsWith("\\")) {
                    defaultValue = defaultValue.replaceAll("\\\\$", "");

                    int nextLineIndex = lineIndex + 1;
                    while (nextLineIndex < lines.size() && lines.get(nextLineIndex).matches("^\\s+.*")) {
                        defaultValue += lines.get(nextLineIndex).trim();
                        if (!defaultValue.endsWith("\\")) {
                            break;
                        }

                        defaultValue = defaultValue.replaceAll("\\\\$", "");
                        nextLineIndex++;
                    }

                    lineIndex = nextLineIndex;
                }

                if (currentProperty.name.startsWith("%")) {
                    System.err.println(Ansi.AUTO.string("""
                            @|yellow [!] Skipping profile-specific property %s|@\
                            """.formatted(currentProperty.name)));
                    currentProperty = new ConfigProperty();
                    continue;
                }

                if (currentProperty.defaultValue == null || currentProperty.defaultValue.isBlank()) {
                    currentProperty.defaultValue = defaultValue;
                } else if (!defaultValue.isEmpty()) {
                    System.err.println(Ansi.AUTO.string("""
                            @|yellow [!] %s has both a default value (%s) and a @default annotation (%s)|@\
                            """.formatted(currentProperty.name, defaultValue, currentProperty.defaultValue)));
                }

                if (!currentProperty.hidden || includeHidden) {
                    properties.add(currentProperty);
                }

                currentProperty = new ConfigProperty();
            }
        }

        return properties;
    }

}
