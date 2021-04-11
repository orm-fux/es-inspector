package com.github.ormfux.esi.model.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ormfux.esi.model.settings.connection.ESConnection;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESIndex implements Comparable<ESIndex> {

    @JsonProperty("pri.store.size")
    private String primaryStoreSize;
    
    @JsonProperty("health")
    private  String health;
    
    @JsonProperty("status")
    private  String status;
    
    @JsonProperty("index")
    private  String name;
    
    @JsonProperty("pri")
    private  String primary;
    
    @JsonProperty("rep")
    private  String rep;
    
    @JsonProperty("docs.count")
    private  String docsCount;
    
    @JsonProperty("docs.deleted")
    private  String deleted;
    
    @JsonProperty("store.size")
    private  String storeSize;
    
    @JsonIgnore
    private ESConnection connection;
    
    @Override
    public int compareTo(final  ESIndex other) {
        return this.name.compareTo(other.name);
    }
}
