package com.github.ormfux.esi.model.alias;

import java.util.List;

import com.github.ormfux.esi.model.settings.connection.ESConnection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESMultiIndexAlias implements Comparable<ESMultiIndexAlias> {
    
    private ESConnection connection;
    
    private String name;
    
    private List<String> indices;
    
    private String writeIndex;

    @Override
    public int compareTo(ESMultiIndexAlias other) {
        return name.compareTo(other.name);
    }
    
}
