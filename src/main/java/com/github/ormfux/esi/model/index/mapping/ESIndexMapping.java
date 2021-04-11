package com.github.ormfux.esi.model.index.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESIndexMapping {
    
    private ESIndexMappingPropertiesSet properties;
    
    private String type;
    
    private ESIndexMappingPropertyFieldSet fields;
    
}
