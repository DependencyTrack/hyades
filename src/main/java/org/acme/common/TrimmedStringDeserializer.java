package org.acme.common;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

public class TrimmedStringDeserializer extends JsonDeserializer<String> {
    public TrimmedStringDeserializer() {
    }

    public String deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode node = (JsonNode)jsonParser.readValueAsTree();
        return StringUtils.trimToNull(node.asText());
    }
}
