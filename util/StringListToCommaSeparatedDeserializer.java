package com.sharkdom.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class StringListToCommaSeparatedDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode element : node) {
                if (builder.length() > 0) builder.append(",");
                builder.append(element.asText());
            }
            return builder.toString();
        } else {
            return node.asText(); // fallback if it's already a string
        }
    }
}
