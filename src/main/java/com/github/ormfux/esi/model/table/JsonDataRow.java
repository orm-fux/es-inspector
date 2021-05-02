package com.github.ormfux.esi.model.table;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class JsonDataRow {
    
    private final JsonNode rowNode;
    
    private final Map<String, String> columnValues = new HashMap<>();

    public void addColumnValue(final String column, final String value) {
        columnValues.put(column, value);
    }

    public String getColumnValue(final String columnName) {
        return columnValues.get(columnName);
    }
    
}
