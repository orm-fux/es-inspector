package com.github.ormfux.esi.ui.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ormfux.esi.service.JsonService;
import com.github.ormfux.simple.di.InjectionContext;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

public class JsonTreeView extends TreeView<String> {
    
    public JsonTreeView() {
        setShowRoot(false);
        
        setCellFactory(tv -> {
            final TreeCell<String> cell = new TextFieldTreeCell<>();
            cell.setFont(Font.font("Courier New"));
            
            return cell;
        });
        
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            final TreeItem<String> selectedItem = getSelectionModel().getSelectedItem();
            
            if(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN).match(e) && selectedItem != null) {
                final JsonNodeTree nodeItem = (JsonNodeTree) selectedItem;
                final JsonNode node = nodeItem.getJsonNode();
                final String clipboardValue;
                
                if (node.isValueNode()) {
                    clipboardValue = node.asText();
                } else {
                    //TODO too lazy to get this properly. do later (sooo, basically never)
                    final JsonService jsonService = InjectionContext.getBean(JsonService.class);
                    clipboardValue = jsonService.writeValueAsPrettyString(node);
                }
                
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(clipboardValue);
                clipboard.setContent(clipboardContent);
            }
        });
    }
    
}
