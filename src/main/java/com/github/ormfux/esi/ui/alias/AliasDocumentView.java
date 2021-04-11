package com.github.ormfux.esi.ui.alias;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AliasDocumentView extends SplitPane {

    private final ComboBox<DocumentAction> actionCombobox = new ComboBox<>();
    
    private final TextField documentIdField = new TextField();
    
    private final TextArea documentInputField = new SourceCodeTextArea();
    
    private final TextArea resultField = new TextArea();
    
    public AliasDocumentView(final AliasDetailsController aliasController) {
        setPadding(new Insets(5));
        
        final VBox documentSubView = new VBox(2);
        
        final ScrollPane documentInputContainer = new ScrollPane(documentInputField);
        documentInputContainer.setFitToHeight(true);
        documentInputContainer.setFitToWidth(true);
        VBox.setVgrow(documentInputContainer, Priority.ALWAYS);
        
        final HBox actionsBox = new HBox(2);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        
        final Label idLabel = new Label("Document Id: ");
        
        final Button searchButton = new Button("Search");
        searchButton.managedProperty().bind(searchButton.visibleProperty());
        searchButton.disableProperty().bind(documentIdField.textProperty().isEmpty());
        searchButton.setOnAction(e -> resultField.setText(aliasController.searchDocument(documentIdField.getText())));
        
        final Button deleteButton = new Button("Delete");
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());
        deleteButton.disableProperty().bind(documentIdField.textProperty().isEmpty());
        deleteButton.setOnAction(e -> {
            final Alert alert = new Alert(AlertType.NONE, "Really delete the document '" + documentIdField.getText() + "'?", ButtonType.CANCEL, ButtonType.OK);
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                resultField.setText(aliasController.deleteDocument(documentIdField.getText()));
            }
        });
        
        final Button saveButton = new Button("Save");
        saveButton.managedProperty().bind(saveButton.visibleProperty());
        saveButton.disableProperty().bind(documentInputField.textProperty().isEmpty().or(documentIdField.textProperty().isEmpty()));
        saveButton.setOnAction(e -> resultField.setText(aliasController.saveDocument(documentIdField.getText(), documentInputField.getText())));
        
        actionsBox.getChildren().addAll(actionCombobox, idLabel, documentIdField, saveButton, searchButton, deleteButton);
        
        actionCombobox.getSelectionModel().selectedItemProperty().addListener((prop, oldSel, newSel) -> {
            documentInputField.setVisible(false);
            deleteButton.setVisible(false);
            searchButton.setVisible(false);
            saveButton.setVisible(false);
            
            switch (newSel) {
                case CHANGE:
                    documentInputField.setVisible(true);
                    saveButton.setVisible(true);
                    
                    break;
                case DELETE:
                    deleteButton.setVisible(true);
                    
                    break;
                case SEARCH:
                    searchButton.setVisible(true);
                    
                    break;
                default:
                    break;
            }
        });
        
        if (aliasController.getAlias().getWriteIndex() != null) {
            actionCombobox.setItems(FXCollections.observableArrayList(DocumentAction.values()));
        } else {
            actionCombobox.setItems(FXCollections.observableArrayList(DocumentAction.SEARCH));
        }
        
        actionCombobox.getSelectionModel().select(DocumentAction.SEARCH);
        
        documentIdField.textProperty().addListener((prop, oldId, newId) -> {
            if (newId != null && actionCombobox.getSelectionModel().getSelectedItem() == DocumentAction.CHANGE) {
                final String existingDocument = aliasController.searchDocumentForUpdate(newId);
                documentInputField.setText(existingDocument);
            } else {
                documentInputField.setText(null);
            }
        });
        
        documentSubView.getChildren().addAll(actionsBox, documentInputContainer);
        
        resultField.setFont(Font.font("Courier New"));
        resultField.setEditable(false);
        final ScrollPane resultContainer = new ScrollPane(resultField);
        resultContainer.setFitToHeight(true);
        resultContainer.setFitToWidth(true);
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.2);
        getItems().addAll(new StackPane(documentSubView), new StackPane(resultContainer));
    }
    
    private static enum DocumentAction {
        SEARCH,
        
        CHANGE,
        
        DELETE
    }
}
