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
package org.dependencytrack.proto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class JacksonProtobufDeserializer<T extends MessageOrBuilder> extends StdDeserializer<T> {

    private final Class<T> clazz;

    public JacksonProtobufDeserializer(final Class<T> clazz) {
        super(clazz);
        this.clazz = clazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final Message.Builder builder;
        try {
            final Object builderObject = clazz.getMethod("newBuilder").invoke(null);
            builder = (Message.Builder) builderObject;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new JsonMappingException(jsonParser, "Failed to create builder from class " + clazz.getName(), e);
        }

        JsonFormat.parser().ignoringUnknownFields().merge(jsonParser.readValueAsTree().toString(), builder);

        return (T) builder.build();
    }

}
