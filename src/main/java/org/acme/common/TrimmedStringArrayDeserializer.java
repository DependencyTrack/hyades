//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.acme.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.lang.Collections;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class TrimmedStringArrayDeserializer extends JsonDeserializer<String[]> {
    public TrimmedStringArrayDeserializer() {
    }

    public String[] deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        List<String> list = new ArrayList();
        JsonNode node = (JsonNode)jsonParser.readValueAsTree();
        if (node.isArray()) {
            Iterator elements = node.elements();

            while(elements.hasNext()) {
                JsonNode childNode = (JsonNode)elements.next();
                String value = StringUtils.trimToNull(childNode.asText());
                if (value != null) {
                    list.add(value);
                }
            }
        }

        return Collections.isEmpty(list) ? null : (String[])list.toArray(new String[list.size()]);
    }
}
