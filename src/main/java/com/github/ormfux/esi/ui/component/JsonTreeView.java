package com.github.ormfux.esi.ui.component;

import javafx.application.Platform;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.text.Font;

public class JsonTreeView extends TreeView<String> {
    
    public JsonTreeView() {
        setShowRoot(false);
        getSelectionModel().selectedItemProperty().addListener((p,o,n) -> {
            if (n != null) {
                Platform.runLater(() -> getSelectionModel().clearSelection());
            }
        });
        
        setCellFactory(tv -> {
            final TreeCell<String> cell = new TextFieldTreeCell<>();
            cell.setFont(Font.font("Courier New"));
            
            cell.indexProperty().addListener((prop, oldIndex, newIndex) -> {
                if (newIndex.intValue() % 2 == 0) {
                    cell.setStyle("-fx-background-color: #f9f9f9");
                } else {
                    cell.setStyle(null);
                }
            });
            
            return cell;
        });
    }
    
}
