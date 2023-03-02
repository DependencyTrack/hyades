package org.hyades.proto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

public class JacksonProtobufSerializer<T extends MessageOrBuilder> extends StdSerializer<T> {

    public JacksonProtobufSerializer(final Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(final T t, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeRawValue(JsonFormat.printer().print(t));
    }

}
