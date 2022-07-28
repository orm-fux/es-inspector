package com.github.ormfux.esi.ui.aliases;

import com.github.ormfux.esi.util.DialogUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.List;

public class DeleteAliasDialog extends Dialog<String> {

    private ComboBox<String> indexCombobox = new ComboBox<>();

    final Text deleteText = new Text();

    public DeleteAliasDialog(final String aliasName, List<String> indices) {
        final DialogPane dialogPane = getDialogPane();
        DialogUtils.configureForOperatingSystem(this);

        dialogPane.setContent(createContentGrid());
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        deleteText.setText("Really delete alias '" + aliasName + "' for selected index?");
        indexCombobox.getItems().addAll(indices);

        final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

        defineInputInteractions(okButton);

        setTitle("Delete Alias for Index");

        Platform.runLater(() -> indexCombobox.requestFocus());

        setResultConverter((dialogButton) -> {
            final ButtonData data =  dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? this.indexCombobox.getSelectionModel().getSelectedItem() : null;
        });
    }

    private Node createContentGrid() {
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setPrefWidth(300);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER);

        grid.addRow(0, deleteText);
        grid.addRow(1, indexCombobox);

        return grid;
    }

    private void defineInputInteractions(final Button okButton) {
        okButton.disableProperty().bind(indexCombobox.getSelectionModel().selectedItemProperty().isNull());
    }

}
