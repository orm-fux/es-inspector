package com.github.ormfux.esi.model.settings.connection;

import lombok.Data;

@Data
public class ESConnection implements Comparable<ESConnection> {

    private String id;
    
    private String name;

    private String url;
    
    private String defaultIndexFilter;
    
    private String defaultAliasFilter;

    private Authentication authentication;

    @Override
    public int compareTo(final ESConnection other) {
        return this.name.compareTo(other.name);
    }
}
