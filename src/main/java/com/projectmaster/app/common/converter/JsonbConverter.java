package com.projectmaster.app.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class JsonbConverter implements AttributeConverter<String, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.trim().isEmpty()) {
            return null;
        }
        
        // If the attribute is already a valid JSON string, use it directly
        if (isValidJson(attribute)) {
            return attribute;
        }
        
        // If it's not valid JSON, treat it as a plain string and wrap it in quotes
        return "\"" + attribute.replace("\"", "\\\"") + "\"";
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
    
    private boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}