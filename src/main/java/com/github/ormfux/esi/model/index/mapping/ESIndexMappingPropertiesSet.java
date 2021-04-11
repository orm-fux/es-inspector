package com.github.ormfux.esi.model.index.mapping;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.Data;

@Data
public class ESIndexMappingPropertiesSet {
    
    private Map<String, ESIndexMapping> properties = new LinkedHashMap<>();
    
    @JsonAnySetter
    public void setProperties(final String key, final ESIndexMapping value) {
        properties.put(key, value);
    }
    
}
