package com.github.ormfux.esi.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class JsonDataTable {
    
    private final Set<String> columns = new HashSet<>();
    
    private final List<JsonDataRow> rows = new ArrayList<>();
    
    public List<String> getOrderedColumns() {
        final List<String> orderedColumns = new ArrayList<>(columns);
        Collections.sort(orderedColumns);
        
        return orderedColumns;
    }
    
    public void addColumn(final String columnName) {
        columns.add(columnName);
    }
    
}
