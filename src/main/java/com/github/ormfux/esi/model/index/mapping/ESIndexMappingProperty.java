package com.github.ormfux.esi.model.index.mapping;

import lombok.Data;

@Data
public class ESIndexMappingProperty implements Comparable<ESIndexMappingProperty> {
    
    private String path;
    
    private String type;
    
    private String analyzer;
    
    private String searchAnalyzer;
    
    @Override
    public int compareTo(final ESIndexMappingProperty other) {
        return this.path.compareTo(other.path);
    }
    
}
