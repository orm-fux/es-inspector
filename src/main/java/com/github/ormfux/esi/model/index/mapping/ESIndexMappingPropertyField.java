package com.github.ormfux.esi.model.index.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ESIndexMappingPropertyField implements Comparable<ESIndexMappingPropertyField> {
    
    private String name;
    
    private String type;

    @Override
    public int compareTo(final ESIndexMappingPropertyField other) {
        return name.compareTo(other.name);
    }
    
}
