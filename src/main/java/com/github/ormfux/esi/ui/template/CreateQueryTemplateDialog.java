package com.github.ormfux.esi.ui.template;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.Data;

public class CreateQueryTemplateDialog extends Dialog<CreateQueryTemplateDialog.TemplateCreateData> {

    private final TemplateCreateData template = new TemplateCreateData();
    
    private final TextField nameField = new TextField();
    
    private final CheckBox globalField = new CheckBox();
    
    public CreateQueryTemplateDialog() {
        setTitle("Create Query Template");
        setResizable(true);
        
        final DialogPane dialogPane = getDialogPane();
//        dialogPane.setMinWidth(550);
//        dialogPane.setMinHeight(300);
        
        dialogPane.setContent(createContentGrid());
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        
        final Button okButton = (Button)dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        okButton.setText("Save");
        
        defineInputInteractions(okButton);
        
        Platform.runLater(() -> nameField.requestFocus());
        
        setResultConverter((dialogButton) -> {
            fillResult();
            
            final ButtonData data =  dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? this.template : null;
        });
    }

    private Node createContentGrid() {
        Label nameLabel = new Label("Name"); 
        nameLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        nameField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setFillWidth(nameField, true);
        
        Label globalLabel = new Label("Global"); 
        globalLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setPrefWidth(300.0);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(globalLabel, 0, 1);
        grid.add(globalField, 1, 1);
        
        return grid;
    }

    private void defineInputInteractions(final Button okButton) {
        okButton.disableProperty().bind(nameField.textProperty().isEmpty());
    }
    
    private void fillResult() {
        template.setName(nameField.getText());
        template.setGlobal(globalField.isSelected());
    }
    
    @Data
    public static class TemplateCreateData {
        private String name;
        
        private boolean global;
    }
    
}
