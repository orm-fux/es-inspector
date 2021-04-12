package com.github.ormfux.esi.model.index.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESIndexMapping {
    
    private ESIndexMappingPropertiesSet properties;
    
    private String type;
    
    private String analyzer;
    
    @JsonProperty("search_analyzer")
    private String searchAnalyzer;
    
    private ESIndexMappingPropertyFieldSet fields;
    
}
