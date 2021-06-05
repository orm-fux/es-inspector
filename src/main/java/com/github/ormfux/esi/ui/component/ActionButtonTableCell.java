package com.github.ormfux.esi.ui.component;

import java.util.function.Function;

import com.github.ormfux.esi.ui.images.ImageButton;
import com.github.ormfux.esi.ui.images.ImageKey;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public class ActionButtonTableCell<S> extends TableCell<S, Button> {

    private final Button actionButton;

    public ActionButtonTableCell(final ImageKey icon, final Function<S, S> action) {
        getStyleClass().add("action-button-table-cell");

        actionButton = new ImageButton(icon);
        actionButton.setOnAction(e -> action.apply(getCurrentItem()));
    }

    public S getCurrentItem() {
        return (S) getTableView().getItems().get(getIndex());
    }

    @Override
    public void updateItem(final Button item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(actionButton);
        }
    }
}