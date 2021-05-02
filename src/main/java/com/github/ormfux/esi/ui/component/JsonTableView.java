package com.github.ormfux.esi.ui.component;

import java.util.List;
import java.util.StringJoiner;

import com.github.ormfux.esi.model.table.JsonDataRow;
import com.github.ormfux.esi.model.table.JsonDataTable;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class JsonTableView extends TableView<JsonDataRow> {

    private JsonDataTable tableData;
    
    public void setTableContent(final JsonDataTable tableData) {
        this.tableData = tableData;
        getItems().clear();
        getColumns().clear();
        
        if (tableData != null) {
            for (final String columnName : tableData.getOrderedColumns()) {
                final TableColumn<JsonDataRow, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().getColumnValue(columnName)));
                getColumns().add(column);
            }
            
            getItems().setAll(tableData.getRows());
        }
    }
    
    public String toCsv() {
        if (tableData != null) {
            final StringJoiner csvJoiner = new StringJoiner("\n");
            
            final List<String> columns = tableData.getOrderedColumns();
            final StringJoiner headerRow = new StringJoiner(",");
            columns.forEach(column -> headerRow.add(escapeSpecialCharacters(column)));
            csvJoiner.add(headerRow.toString());
            
            for (final JsonDataRow row : tableData.getRows()) {
                final StringJoiner csvRow = new StringJoiner(",");
                
                columns.forEach(column -> {
                    final String columnValue = row.getColumnValue(column);
                    
                    if (columnValue != null) {
                        csvRow.add(escapeSpecialCharacters(columnValue));
                    } else {
                        csvRow.add("");
                    }
                });
                
                csvJoiner.add(csvRow.toString());
            }
            
            return csvJoiner.toString();
        } else {
            return "";
        }
    }
    
    
    private String escapeSpecialCharacters(final String data) {
        String escapedData = data.replaceAll("\\R", " ");
        
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            escapedData = escapedData.replace("\"", "\"\"");
            escapedData = "\"" + escapedData + "\"";
        }
        
        return escapedData;
    }
}
