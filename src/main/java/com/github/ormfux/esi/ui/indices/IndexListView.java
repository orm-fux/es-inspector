package com.github.ormfux.esi.ui.indices;

import com.github.ormfux.esi.controller.ManageIndicesController;
import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.ui.images.ImageButton;
import com.github.ormfux.esi.ui.images.ImageKey;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

public class IndexListView extends VBox {
    
    private final TextField filterField = new TextField();
    
    public IndexListView(final ManageIndicesController indicesController) {
        setPadding(new Insets(2));
        
        final ListView<ESIndex> viewContent = new ListView<>(indicesController.getAllIndices());
        viewContent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        viewContent.setCellFactory(view -> new IndexListCell());
        
        viewContent.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                final ESIndex selectedIndex = viewContent.getSelectionModel().getSelectedItem();
                
                if (selectedIndex != null) {
                    indicesController.openIndexDetails(selectedIndex);
                }
            }
        });
        
        final BorderPane viewHeader = new BorderPane();
        viewHeader.setPadding(new Insets(2));

        final Label headerLabel = new Label("Indices");
        viewHeader.setLeft(headerLabel);
        BorderPane.setAlignment(headerLabel, Pos.CENTER_LEFT);

        final Label connectionNameLabel = new Label();
        connectionNameLabel.setTextFill(Color.GREEN);
        viewHeader.setCenter(connectionNameLabel);

        final HBox actionsContainer = new HBox(2);
        
        final Button createButton = new ImageButton(ImageKey.CREATE);
        createButton.setDisable(true);
        createButton.setOnAction(e -> new CreateIndexDialog().showAndWait().ifPresent(idx -> indicesController.createIndex(idx.getName(), idx.getProperties())));
        
        final Button deleteButton = new ImageButton(ImageKey.DELETE);
        deleteButton.setDisable(true);
        deleteButton.disableProperty().bind(viewContent.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(e -> {
            final ESIndex selectedIndex = viewContent.getSelectionModel().getSelectedItem();
            final Alert alert = new Alert(AlertType.NONE, "Really delete the index '" + selectedIndex.getName() + "'?", ButtonType.CANCEL, ButtonType.OK);
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                indicesController.deleteIndex(selectedIndex);
            }
        });
        
        final Button openButton = new ImageButton(ImageKey.CONNECT);
        openButton.setDisable(true);
        openButton.disableProperty().bind(viewContent.getSelectionModel().selectedItemProperty().isNull());
        openButton.setOnAction(e -> indicesController.openIndexDetails(viewContent.getSelectionModel().getSelectedItem()));
        
        indicesController.getSelectedConnection().addListener((prop, oldConn, newConn) -> {
            filterField.setText(null);
            
            if (newConn != null) {
                filterField.setText(newConn.getDefaultIndexFilter());
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

        filterField.textProperty().addListener((prop, oldText, newText) -> filterIndices(indicesController, viewContent, newText));
        viewFooter.setRight(filterField);

        getChildren().addAll(viewHeader, viewContent, viewFooter);
    }

    private void filterIndices(final ManageIndicesController indicesController, final ListView<ESIndex> viewContent, final String filterText) {
        viewContent.setItems(indicesController.getAllIndices().filtered(index -> filterText == null || index.getName().contains(filterText)));
    }
    
    private static class IndexListCell extends ListCell<ESIndex> {
        @Override
        protected void updateItem(final ESIndex index, final boolean empty) {
            super.updateItem(index, empty);

            if(empty || index == null) {
                setText(null);
                setGraphic(null);
                
            } else {
                setText(null);
                setGraphic(new Label(String.valueOf(index.getName())));
            }
        }
    }
}
