package org.acme.serde;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.model.ComponentAnalysisCache;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class CustomJsonDeserializer extends JsonDeserializer<String> {
    //private static final TypeReference<Map<String,String>> MAP_STRING_OBJECT = new TypeReference<Map<String, Object>>(){ /* empty */ };
    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return jsonParser.readValueAsTree().toString();
}



}
