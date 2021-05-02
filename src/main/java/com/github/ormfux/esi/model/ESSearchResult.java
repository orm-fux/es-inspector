package com.github.ormfux.esi.model;

import com.github.ormfux.esi.model.table.JsonDataTable;
import com.github.ormfux.esi.ui.component.JsonNodeTree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ESSearchResult {
    
    private final String resultString;
    
    private final JsonNodeTree fxTree;
    
    private final JsonDataTable tableData;
    
}
