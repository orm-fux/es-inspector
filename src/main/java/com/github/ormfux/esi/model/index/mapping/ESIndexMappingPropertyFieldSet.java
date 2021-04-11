package com.github.ormfux.esi.model.index.mapping;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.Data;

@Data
public class ESIndexMappingPropertyFieldSet {
    
    private Map<String, ESIndexMapping> fields = new LinkedHashMap<>();
    
    @JsonAnySetter
    public void setFields(final String key, final ESIndexMapping value) {
        fields.put(key, value);
    }
    
}
