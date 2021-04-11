package com.github.ormfux.esi.model.alias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESSingleIndexAlias {
    
    @JsonProperty("alias")
    private String name;
    
    @JsonProperty("index")
    private String index;
    
    @JsonProperty("is_write_index")
    private String writeIndex;
    
    @JsonProperty("filter")
    private String filter;
    
}
