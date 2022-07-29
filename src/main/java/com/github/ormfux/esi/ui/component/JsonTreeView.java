package com.github.ormfux.esi.ui.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ormfux.esi.service.JsonService;
import com.github.ormfux.simple.di.InjectionContext;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
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

import java.util.Optional;

public class JsonTreeView extends TreeView<String> {

    public JsonTreeView() {
        setShowRoot(false);

        setCellFactory(tv -> {
            final TreeCell<String> cell = new TextFieldTreeCell<>();
            cell.setFont(Font.font("Courier New"));

            return cell;
        });

        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        configureTreeNodeToClipboard();
        Platform.runLater(() -> configureScrolling());
    }

    public void setTree(final JsonNodeTree tree) {
        Platform.runLater(() -> {
            setRoot(tree);
            scrollTo(0);
        });
    }

    private void configureTreeNodeToClipboard() {
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

    private void configureScrolling() {
        final Optional<ScrollBar> verticalScrollBar = this.lookupAll(".scroll-bar")
                .stream()
                .filter(node -> node instanceof ScrollBar)
                .map(node -> (ScrollBar) node)
                .filter(scrollbar -> scrollbar.getOrientation() == Orientation.VERTICAL)
                .findFirst();

        if (verticalScrollBar.isPresent()) {
            verticalScrollBar.get().valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((double) newValue > (double) oldValue && (double) newValue == 1.0) {
                    final boolean batchLoaded = ((JsonNodeTree) getRoot()).loadNextLazyChildrenBatch();

                    if (batchLoaded) {
                        verticalScrollBar.get().setValue((double) oldValue);
                    }
                }
            });
        }
    }

}
