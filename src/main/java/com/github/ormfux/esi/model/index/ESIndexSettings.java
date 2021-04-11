package com.github.ormfux.esi.model.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESIndexSettings {
    
    @JsonIgnore
    private String rawSettings;
    
    @JsonProperty("index.creation_date")
    private String creationDate;
    
    @JsonProperty("index.number_of_replicas")
    private String replicas;
    
    @JsonProperty("index.number_of_shards")
    private String shards;
    
    @JsonProperty("index.provided_name")
    private String providedName;
    
    @JsonProperty("index.uuid")
    private String uuid;
    
}
