package com.github.ormfux.esi.model.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryTemplate implements Comparable<QueryTemplate> {

    private String id;
    
    private String name;
    
    private String query;

    @Override
    public int compareTo(QueryTemplate other) {
        return name.compareTo(other.name);
    }
}
