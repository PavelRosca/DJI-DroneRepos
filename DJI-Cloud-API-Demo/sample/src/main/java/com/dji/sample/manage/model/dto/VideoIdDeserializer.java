package com.dji.sample.manage.model.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom deserializer for video_id field to handle both string and object inputs
 */
public class VideoIdDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        
        // If it's a text node (string), return as-is
        if (node.isTextual()) {
            return node.asText();
        }
        
        // If it's an object node, convert to Map for VideoId constructor
        if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isTextual()) {
                    map.put(entry.getKey(), value.asText());
                } else if (value.isNumber()) {
                    map.put(entry.getKey(), value.asInt());
                } else if (value.isObject()) {
                    map.put(entry.getKey(), value);
                } else {
                    map.put(entry.getKey(), value.asText());
                }
            });
            return map;
        }
        
        // Otherwise return as-is
        return node.asText();
    }
}
