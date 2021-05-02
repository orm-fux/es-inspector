package com.github.ormfux.esi.ui.component;

import com.github.ormfux.esi.model.table.JsonDataRow;
import com.github.ormfux.esi.model.table.JsonDataTable;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class JsonTableView extends TableView<JsonDataRow> {

    public void setTableContent(final JsonDataTable tableData) {
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
    
}
