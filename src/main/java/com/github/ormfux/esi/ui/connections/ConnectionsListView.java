package com.github.ormfux.esi.ui.connections;

import com.github.ormfux.esi.controller.ManageAliasesController;
import com.github.ormfux.esi.controller.ManageConnectionsController;
import com.github.ormfux.esi.controller.ManageIndicesController;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
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

public class ConnectionsListView extends VBox {
    
    private final TextField filterField = new TextField();
    
    public ConnectionsListView(final ManageConnectionsController connectionsController, 
                               final ManageIndicesController indicesController,
                               final ManageAliasesController aliasesController) {
        setPadding(new Insets(2));
        
        final ListView<ESConnection> viewContent = new ListView<>(connectionsController.getAllConnections());
        viewContent.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        viewContent.setCellFactory(view -> new ConnectionListCell());
        
        final BorderPane viewHeader = new BorderPane();
        viewHeader.setPadding(new Insets(2));

        final Label headerLabel = new Label("Connections");
        viewHeader.setLeft(headerLabel);
        BorderPane.setAlignment(headerLabel, Pos.CENTER_LEFT);

        final HBox actionsContainer = new HBox(2);
        final Button createButton = new ImageButton(ImageKey.CREATE);
        createButton.setOnAction(e -> new ConnectionDialog(null, con -> connectionsController.lookupElasticsearchVersion(con)).showAndWait().ifPresent(connectionsController::createConnection));
        
        final Button deleteButton = new ImageButton(ImageKey.DELETE);
        deleteButton.setDisable(true);
        //TODO would like to use binding, but then can't react to "isConnected" change
        //deleteButton.disableProperty().bind(viewContent.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.setOnAction(e -> {
            final ESConnection selectedConnection = viewContent.getSelectionModel().getSelectedItem();
            final Alert alert = new Alert(AlertType.NONE, "Really delete the connection '" + selectedConnection.getName() + "'?", ButtonType.CANCEL, ButtonType.OK);
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                connectionsController.deleteConnection(selectedConnection);
            }
        });
        
        final Button editButton = new ImageButton(ImageKey.EDIT);
        editButton.setDisable(true);
        //editButton.disableProperty().bind(viewContent.getSelectionModel().selectedItemProperty().isNull());
        editButton.setOnAction(e -> {
            final ESConnection selectedConnection = viewContent.getSelectionModel().getSelectedItem();
            new ConnectionDialog(selectedConnection, con -> connectionsController.lookupElasticsearchVersion(con)).showAndWait().ifPresent(connectionsController::editConnection);
        });
        
        final Button openButton = new ImageButton(ImageKey.CONNECT);
        openButton.setDisable(true);
        //openButton.disableProperty().bind(viewContent.getSelectionModel().selectedItemProperty().isNull());
        openButton.setOnAction(e -> {
            deleteButton.setDisable(true);
            editButton.setDisable(true);
            final ESConnection selectedConnection = viewContent.getSelectionModel().getSelectedItem();
            indicesController.getSelectedConnection().set(selectedConnection);
            aliasesController.getSelectedConnection().set(selectedConnection);
        });
        
        final Button tgmButton = new ImageButton(ImageKey.GOD_MODE);
        tgmButton.setDisable(true);
        tgmButton.setOnAction(e -> {
            deleteButton.setDisable(true);
            editButton.setDisable(true);
            final ESConnection selectedConnection = viewContent.getSelectionModel().getSelectedItem();
            connectionsController.openGodModeView(selectedConnection);
        });
        
        viewContent.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                final ESConnection selectedConnection = viewContent.getSelectionModel().getSelectedItem();
                
                if (selectedConnection != null) {
                    deleteButton.setDisable(true);
                    editButton.setDisable(true);
                    indicesController.getSelectedConnection().set(selectedConnection);
                    aliasesController.getSelectedConnection().set(selectedConnection);
                }
            }
        });
        
        viewContent.getSelectionModel().selectedItemProperty().addListener((prop, oldConn, newConn) -> {
            if (oldConn != null && newConn == null) {
                indicesController.getSelectedConnection().set(null);
                aliasesController.getSelectedConnection().set(null);
            }
            
            if (newConn != null) {
                openButton.setDisable(false);
                tgmButton.setDisable(false);
                
                if (connectionsController.isUsed(newConn)) {
                    deleteButton.setDisable(true);
                    editButton.setDisable(true);
                } else {
                    deleteButton.setDisable(false);
                    editButton.setDisable(false);
                }
            } else {
                tgmButton.setDisable(true);
                openButton.setDisable(true);
                deleteButton.setDisable(true);
                editButton.setDisable(true);
            }
        });
        
        actionsContainer.getChildren().addAll(openButton, tgmButton, createButton, editButton, deleteButton);
        viewHeader.setRight(actionsContainer);

        final BorderPane viewFooter = new BorderPane();
        viewFooter.setPadding(new Insets(2));

        final Label filterLabel = new Label("Filter");
        viewFooter.setCenter(filterLabel);

        filterField.setOnAction(e -> viewContent.setItems(connectionsController.getAllConnections().filtered(conn -> filterField.getText() == null || conn.getName().contains(filterField.getText()))));
        viewFooter.setRight(filterField);

        getChildren().addAll(viewHeader, viewContent, viewFooter);
    }
    
    private static class ConnectionListCell extends ListCell<ESConnection> {
        @Override
        protected void updateItem(final ESConnection connection, final boolean empty) {
            super.updateItem(connection, empty);

            if(empty || connection == null) {
                setText(null);
                setGraphic(null);
                
            } else {
                setText(null);
                setGraphic(new Label(String.valueOf(connection.getName())));
            }
        }
    }
    
}
