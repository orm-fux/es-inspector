package com.github.ormfux.esi.ui.template;

import com.github.ormfux.esi.model.template.QueryTemplate;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;

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

public class EditQueryTemplateDialog extends Dialog<QueryTemplate> {

    private final QueryTemplate template;
    
    private final TextField nameField = new TextField();
    
    private final TextArea queryField = new SourceCodeTextArea();
    
    public EditQueryTemplateDialog(final QueryTemplate template) {
        setTitle("Edit Query Template");
        setResizable(true);
        this.template = template;
        nameField.setText(template.getName());
        queryField.setText(template.getQuery());
        
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setMinWidth(550);
        dialogPane.setMinHeight(300);
        
        dialogPane.setContent(createContentGrid());
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        
        final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
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
        
        Label queryLabel = new Label("Query"); 
        queryLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        GridPane.setHgrow(queryField, Priority.ALWAYS);
        GridPane.setVgrow(queryField, Priority.ALWAYS);
        GridPane.setFillWidth(queryField, true);
        GridPane.setFillHeight(queryField, true);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setPrefWidth(300.0);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(queryLabel, 0, 1);
        grid.add(queryField, 1, 1);
        
        return grid;
    }

    private void defineInputInteractions(final Button okButton) {
        okButton.disableProperty().bind(nameField.textProperty().isEmpty().or(queryField.textProperty().isEmpty()));
    }
    
    private void fillResult() {
        template.setName(nameField.getText());
        template.setQuery(queryField.getText());
    }
}
