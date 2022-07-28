package com.github.ormfux.esi.ui.aliases;

import com.github.ormfux.esi.model.alias.ESSingleIndexAlias;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;
import com.github.ormfux.esi.util.DialogUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.function.Supplier;

public class CreateAliasDialog extends Dialog<ESSingleIndexAlias> {

    private final ESSingleIndexAlias alias;

    private TextField nameField = new TextField();

    private ComboBox<String> indexCombobox = new ComboBox<>();

    private CheckBox writeIndexCheckbox = new CheckBox();

    private TextArea filterTextField = new SourceCodeTextArea();

    public CreateAliasDialog(final Supplier<List<String>> indicesSupplier) {
        this.alias = new ESSingleIndexAlias();
        DialogUtils.configureForOperatingSystem(this);

        final DialogPane dialogPane = getDialogPane();

        dialogPane.setContent(createContentGrid());
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        indexCombobox.getItems().addAll(indicesSupplier.get());

        final Button okButton = (Button)dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        okButton.setText("Save");

        defineInputInteractions(okButton);

        setTitle("Create or Edit Alias");

        Platform.runLater(() -> nameField.requestFocus());

        setResultConverter((dialogButton) -> {
            fillResult(this.alias);

            final ButtonData data =  dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? this.alias : null;
        });
    }


    private Node createContentGrid() {
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        //grid.setPrefWidth(300);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);

        final Label nameLabel = new Label("Name");
        nameLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        nameField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setFillWidth(nameField, true);
        grid.addRow(0, nameLabel, nameField);

        final Label indexLabel = new Label("Index");
        indexLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        indexCombobox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(indexCombobox, Priority.ALWAYS);
        GridPane.setFillWidth(indexCombobox, true);
        grid.addRow(1, indexLabel, indexCombobox);

        final Label writeIndexLabel = new Label("Is Write Index?");
        writeIndexLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        writeIndexCheckbox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(writeIndexCheckbox, Priority.ALWAYS);
        GridPane.setFillWidth(writeIndexCheckbox, true);
        grid.addRow(2, writeIndexLabel, writeIndexCheckbox);

        final Label filterLabel = new Label("Filter");
        filterLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        filterTextField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(filterTextField, Priority.ALWAYS);
        GridPane.setFillWidth(filterTextField, true);
        grid.addRow(3, filterLabel, filterTextField);

        return grid;
    }

    private void defineInputInteractions(final Button okButton) {
        okButton.disableProperty().bind(nameField.textProperty().isEmpty().or(indexCombobox.getSelectionModel().selectedItemProperty().isNull()));
    }

    private void fillResult(final ESSingleIndexAlias filledAlias) {
        filledAlias.setName(nameField.getText());
        filledAlias.setFilter(filterTextField.getText());
        filledAlias.setIndex(indexCombobox.getSelectionModel().getSelectedItem());
        filledAlias.setWriteIndex(writeIndexCheckbox.isSelected() ? "true" : "false");
    }

}
