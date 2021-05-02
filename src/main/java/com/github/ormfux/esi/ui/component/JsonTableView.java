package com.github.ormfux.esi.ui.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

import com.github.ormfux.esi.model.table.JsonDataRow;
import com.github.ormfux.esi.model.table.JsonDataTable;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class JsonTableView extends Pagination {

    private JsonDataTable tableData;
    
    private final TableView<JsonDataRow> paginatedTable = new TableView<>();
    
    private final int pageSize;
    
    public JsonTableView(final int pageSize) {
        super(1, 0);
        this.pageSize = pageSize;
        
        setMaxPageIndicatorCount(7);
        
        setPageFactory(pageIndex -> {
            fillPage(pageIndex);
            return paginatedTable;
        });
        
        paginatedTable.setSortPolicy(table -> {
            if (tableData == null || tableData.getRows().isEmpty()) {
                return true;
            }

            final Comparator<JsonDataRow> comparator = table.getComparator();
            
            if (comparator == null) {
                return true;
            }
            
            Collections.sort(tableData.getRows(), comparator);
            
            fillPage(getCurrentPageIndex());
            
            return true;
        });
    }
    
    public void setTableContent(final JsonDataTable tableData) {
        this.tableData = tableData;
        paginatedTable.getItems().clear();
        paginatedTable.getColumns().clear();
        
        if (tableData != null) {
            for (final String columnName : tableData.getOrderedColumns()) {
                final TableColumn<JsonDataRow, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().getColumnValue(columnName)));
                
                paginatedTable.getColumns().add(column);
            }
            
            final int rowCount = tableData.getRows().size();
            final int pageCount = (rowCount / pageSize) + (rowCount % pageSize == 0 ? 0 : 1);
            
            if (pageCount == 0) {
                setPageCount(1);
            } else {
                setPageCount(pageCount);
            }
            
        } else {
            setPageCount(1);
        }
        
        setCurrentPageIndex(0);
    }
    
    private void fillPage(final int pageIndex) {
        if (tableData != null) {
            final int startIndex = pageIndex * pageSize;
            final int endIndex = (pageIndex +1) * pageSize;
            
            final List<JsonDataRow> rowData = tableData.getRows();
            
            paginatedTable.getItems().setAll(rowData.subList(startIndex, endIndex > rowData.size() ? rowData.size() : endIndex));
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
        String escapedData = data.replaceAll("\\r|\\n", " ");
        
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            escapedData = escapedData.replace("\"", "\"\"");
            escapedData = "\"" + escapedData + "\"";
        }
        
        return escapedData;
    }
}
