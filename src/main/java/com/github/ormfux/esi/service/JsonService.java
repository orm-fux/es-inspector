package com.github.ormfux.esi.service;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.ui.component.JsonNodeTree;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

@Bean
public class JsonService {
    
    private final ObjectMapper jsonMapper;
    
    @BeanConstructor
    public JsonService() {
        jsonMapper = new ObjectMapper();
        jsonMapper.activateDefaultTyping(jsonMapper.getPolymorphicTypeValidator());
    }
    
    public JsonNodeTree createJsonFXTree(final String json, final int initialExpandedLevels) {
        try {
            final JsonNode jsonRootNote = jsonMapper.readTree(json);
            
            return new JsonNodeTree(jsonRootNote, initialExpandedLevels);
        } catch (JsonProcessingException e) {
            //swallow this Exception for easy hiding in UI.
            return null;
        }
    }
    
    public String findNodeAsString(final String json, final String nodeName) {
        try {
            final JsonNode responseTree = jsonMapper.readTree(json);
            final JsonNode sourceNode = responseTree.findValue(nodeName);
            
            if (sourceNode != null) {
                return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sourceNode);
            } else {
                return null;
            }
            
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Error processing JSON data.", e);
        }
    }
    
    public String readStringFromPath(final String json, final String path) {
        try {
            return jsonMapper.readTree(json).at(path).textValue();
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Error processing JSON data.", e);
        }
    }
    
    public JsonNode readTreeFromPath(final String json, final String path) {
        try {
            return jsonMapper.readTree(json).at(path);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Error processing JSON data.", e);
        }
    }
    
    public void writeToFile(final File file, final Object value) {
        try {
            jsonMapper.writeValue(file, value);
        } catch (IOException e) {
            throw new ApplicationException("Error serializing JSON data.", e);
        }
    }
    
    public String writeValueAsString(final Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Error serializing JSON data.", e);
        }
    }
    
    public <T> T readValueFromFile(final File file, Class<T> type) {
        try {
            return jsonMapper.readValue(file, type);
        } catch (IOException e) {
            throw new ApplicationException("Error deserializing JSON data.", e);
        }
    }
    
    public <T> T readValueFromString(final String json, Class<T> type) {
        try {
            return jsonMapper.readValue(json, type);
        } catch (IOException e) {
            throw new ApplicationException("Error deserializing JSON data.", e);
        }
    }
    
}
