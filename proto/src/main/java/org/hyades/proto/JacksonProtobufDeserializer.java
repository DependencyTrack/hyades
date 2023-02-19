package org.hyades.proto;

import com.fasterxml.jackson.core.JacksonException;
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
    public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JacksonException {
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
