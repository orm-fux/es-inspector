package com.github.ormfux.esi.ui.index;

import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.ui.component.AsyncButton;
import com.github.ormfux.esi.ui.component.JsonTreeView;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class IndexDocumentView extends SplitPane {

    private final ComboBox<DocumentAction> actionCombobox = new ComboBox<>(FXCollections.observableArrayList(DocumentAction.values()));
    
    private final TextField documentIdField = new TextField();
    
    private final TextArea documentInputField = new SourceCodeTextArea();
    
    private final TextArea rawResultField = new SourceCodeTextArea();
    
    private final JsonTreeView treeResultField = new JsonTreeView(); 
    
    public IndexDocumentView(final IndexDetailsController indexController) {
        setPadding(new Insets(5));
        
        final VBox documentSubView = new VBox(2);
        
        final ScrollPane documentInputContainer = new ScrollPane(documentInputField);
        documentInputContainer.setFitToHeight(true);
        documentInputContainer.setFitToWidth(true);
        VBox.setVgrow(documentInputContainer, Priority.ALWAYS);
        
        final HBox actionsBox = new HBox(2);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        
        final Label idLabel = new Label("Document Id: ");
        
        final AsyncButton searchButton = new AsyncButton("Search");
        final Node runningIcon = searchButton.getRunningIndicator();
        searchButton.managedProperty().bind(searchButton.visibleProperty());
        searchButton.disableProperty().bind(documentIdField.textProperty().isEmpty().or(runningIcon.visibleProperty()));
        searchButton.setAction(() -> {
            final ESSearchResult searchResult = indexController.searchDocument(documentIdField.getText());
            rawResultField.setText(searchResult.getResultString());
            treeResultField.setTree(searchResult.getFxTree());
        });
        
        final AsyncButton deleteButton = new AsyncButton("Delete", runningIcon);
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());
        deleteButton.disableProperty().bind(documentIdField.textProperty().isEmpty().or(runningIcon.visibleProperty()));
        deleteButton.setAction(() -> {
            final Alert alert = new Alert(AlertType.NONE, "Really delete the document '" + documentIdField.getText() + "'?", ButtonType.CANCEL, ButtonType.OK);
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                rawResultField.setText(indexController.deleteDocument(documentIdField.getText()));
                treeResultField.setTree(null);
            }
        });
        
        final AsyncButton saveButton = new AsyncButton("Save");
        saveButton.managedProperty().bind(saveButton.visibleProperty());
        saveButton.disableProperty().bind(documentInputField.textProperty().isEmpty().or(documentIdField.textProperty().isEmpty()).or(runningIcon.visibleProperty()));
        saveButton.setAction(() -> {
            rawResultField.setText(indexController.saveDocument(documentIdField.getText(), documentInputField.getText()));
            treeResultField.setTree(null);
        });
        
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
        
        actionCombobox.getSelectionModel().select(DocumentAction.SEARCH);
        
        documentIdField.textProperty().addListener((prop, oldId, newId) -> {
            if (newId != null && !newId.isBlank() && actionCombobox.getSelectionModel().getSelectedItem() == DocumentAction.CHANGE) {
                final String existingDocument = indexController.searchDocumentForUpdate(newId);
                documentInputField.setText(existingDocument);
            } else {
                documentInputField.setText(null);
            }
        });
        
        documentSubView.getChildren().addAll(actionsBox, documentInputContainer);
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.3);
        getItems().addAll(documentSubView, createResultSubView());
    }
    
    private TabPane createResultSubView() {
        final TabPane view = new TabPane();
        view.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        final Tab rawResultTab = new Tab("Raw");
        rawResultField.setEditable(false);
        final ScrollPane rawScroll = new ScrollPane(rawResultField);
        rawScroll.setFitToHeight(true);
        rawScroll.setFitToWidth(true);
        rawResultTab.setContent(rawScroll);
        
        view.getTabs().add(rawResultTab);
        
        final Tab treeResultTab = new Tab("Tree");

        final ScrollPane treeScroll = new ScrollPane(treeResultField);
        treeScroll.setFitToHeight(true);
        treeScroll.setFitToWidth(true);
        treeResultTab.setContent(treeScroll);
        
        view.getTabs().add(treeResultTab);
        view.getSelectionModel().select(rawResultTab);
        
        return view;
    }
    
    private static enum DocumentAction {
        SEARCH,
        
        CHANGE,
        
        DELETE
    }
}
