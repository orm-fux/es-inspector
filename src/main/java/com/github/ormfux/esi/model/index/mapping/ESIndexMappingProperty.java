package com.github.ormfux.esi.model.index.mapping;

import java.util.List;

import lombok.Data;

@Data
public class ESIndexMappingProperty implements Comparable<ESIndexMappingProperty> {
    
    private String path;
    
    private String type;
    
    private List<ESIndexMappingPropertyField> fields;

    @Override
    public int compareTo(final ESIndexMappingProperty other) {
        return this.path.compareTo(other.path);
    }
    
}
