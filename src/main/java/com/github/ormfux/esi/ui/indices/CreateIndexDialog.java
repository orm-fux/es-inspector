package com.github.ormfux.esi.ui.indices;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import lombok.Data;

public class CreateIndexDialog extends Dialog<CreateIndexDialog.CreateData> {

    private final CreateData index;
    
    private TextField nameField = new TextField();
    
    private final TextArea propertiesField = new TextArea();
    
    public CreateIndexDialog() {
        this.index = new CreateData();
        
        setTitle("Create Index");
        setResizable(true);
        
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setMinWidth(550);
        dialogPane.setMinHeight(300);
        
        dialogPane.setContent(createContentGrid());
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        
        final Button okButton = (Button)dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        okButton.setText("Save");
        
        final Button cancelButton = (Button)dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Cancel");
        
        defineInputInteractions(okButton);
        
        Platform.runLater(() -> nameField.requestFocus());
        
        setResultConverter((dialogButton) -> {
            fillResult();
            
            final ButtonData data =  dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? this.index : null;
        });
    }

    private Node createContentGrid() {
        Label nameLabel = new Label("Name"); 
        nameLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        nameField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setFillWidth(nameField, true);
        
        Label propertiesLabel = new Label("Properties"); 
        propertiesLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        propertiesField.setMaxWidth(Double.MAX_VALUE);
        propertiesField.setFont(Font.font("Courier New"));
        GridPane.setHgrow(propertiesField, Priority.ALWAYS);
        GridPane.setVgrow(propertiesField, Priority.ALWAYS);
        GridPane.setFillWidth(propertiesField, true);
        GridPane.setFillHeight(propertiesField, true);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setPrefWidth(300.0);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(propertiesLabel, 0, 1);
        grid.add(propertiesField, 1, 1);
        
        return grid;
    }

    private void defineInputInteractions(final Button okButton) {
        okButton.disableProperty().bind(nameField.textProperty().isEmpty());
    }
    
    private void fillResult() {
        index.setName(nameField.getText());
        index.setProperties(propertiesField.getText());
    }
    
    @Data
    protected static class CreateData {
        private String name;
        
        private String properties;
    }
    
}
