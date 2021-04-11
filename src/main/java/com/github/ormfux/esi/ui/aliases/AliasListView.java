package com.github.ormfux.esi.ui.aliases;

import com.github.ormfux.esi.controller.ManageAliasesController;
import com.github.ormfux.esi.model.alias.ESMultiIndexAlias;
import com.github.ormfux.esi.ui.images.ImageButton;
import com.github.ormfux.esi.ui.images.ImageKey;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class AliasListView extends VBox {
    
    private final TextField filterField = new TextField();
    
    public AliasListView(final ManageAliasesController aliasesController) {
        setPadding(new Insets(2));
        
        final ListView<ESMultiIndexAlias> viewContent = new ListView<>(aliasesController.getAllAliases());
        viewContent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        viewContent.setCellFactory(view -> new AliasListCell());
        
        viewContent.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                final ESMultiIndexAlias selectedAlias = viewContent.getSelectionModel().getSelectedItem();
                
                if (selectedAlias != null) {
                    aliasesController.openAliasDetails(selectedAlias);
                }
            }
        });
        
        final BorderPane viewHeader = new BorderPane();
        viewHeader.setPadding(new Insets(2));

        final Label headerLabel = new Label("Aliases");
        viewHeader.setLeft(headerLabel);
        BorderPane.setAlignment(headerLabel, Pos.CENTER_LEFT);

        final Label connectionNameLabel = new Label();
        connectionNameLabel.setTextFill(Color.GREEN);
        viewHeader.setCenter(connectionNameLabel);

        final HBox actionsContainer = new HBox(2);
        
        final Button createButton = new ImageButton(ImageKey.CREATE);
        createButton.setDisable(true);
        createButton.setOnAction(e -> new CreateAliasDialog(() -> aliasesController.loadAllIndexNames())
                                        .showAndWait()
                                        .ifPresent(alias -> aliasesController.createAlias(alias)));
        
        final Button deleteButton = new ImageButton(ImageKey.DELETE);
        deleteButton.setDisable(true);
        deleteButton.disableProperty().bind(viewContent.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(e -> {
            final ESMultiIndexAlias selectedAlias = viewContent.getSelectionModel().getSelectedItem();
            final Dialog<String> alert = new DeleteAliasDialog(selectedAlias.getName(), selectedAlias.getIndices());
            
            alert.showAndWait().ifPresent(index -> aliasesController.deleteAlias(selectedAlias.getName(), index));
        });
        
        final Button openButton = new ImageButton(ImageKey.CONNECT);
        openButton.setDisable(true);
        openButton.disableProperty().bind(viewContent.getSelectionModel().selectedItemProperty().isNull());
        openButton.setOnAction(e -> aliasesController.openAliasDetails(viewContent.getSelectionModel().getSelectedItem()));
        
        aliasesController.getSelectedConnection().addListener((prop, oldConn, newConn) -> {
            filterField.setText(null);
            if (newConn != null) {
                filterField.setText(newConn.getDefaultAliasFilter());
                connectionNameLabel.setText(newConn.getName());
                createButton.setDisable(false);
            } else {
                connectionNameLabel.setText(null);
                createButton.setDisable(true);
            }
        });
        
        actionsContainer.getChildren().addAll(openButton, createButton, deleteButton);
        viewHeader.setRight(actionsContainer);

        final BorderPane viewFooter = new BorderPane();
        viewFooter.setPadding(new Insets(2));

        final Label filterLabel = new Label("Filter");
        viewFooter.setCenter(filterLabel);

        filterField.textProperty().addListener((prop, oldText, newText) -> filterAliases(aliasesController, viewContent, newText));
        viewFooter.setRight(filterField);

        getChildren().addAll(viewHeader, viewContent, viewFooter);
    }

    private void filterAliases(final ManageAliasesController aliasesController, final ListView<ESMultiIndexAlias> viewContent, final String newText) {
        viewContent.setItems(aliasesController.getAllAliases().filtered(alias -> newText == null || alias.getName().contains(newText)));
    }
    
    private static class AliasListCell extends ListCell<ESMultiIndexAlias> {
        @Override
        protected void updateItem(final ESMultiIndexAlias alias, final boolean empty) {
            super.updateItem(alias, empty);

            if(empty || alias == null) {
                setText(null);
                setGraphic(null);
                
            } else {
                setText(null);
                setGraphic(new Label(alias.getName()));
            }
        }
    }
}
