package com.github.ormfux.esi.ui;

import com.github.ormfux.esi.controller.ManageAliasesController;
import com.github.ormfux.esi.controller.ManageConnectionsController;
import com.github.ormfux.esi.controller.ManageIndicesController;
import com.github.ormfux.esi.ui.aliases.AliasListView;
import com.github.ormfux.esi.ui.connections.ConnectionsListView;
import com.github.ormfux.esi.ui.indices.IndexListView;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ConnectionSelectionView extends VBox {

    public ConnectionSelectionView(final ManageConnectionsController connectionsController, 
                                   final ManageIndicesController indicesController,
                                   final ManageAliasesController aliasesController) {
        setPadding(new Insets(2));

        final ConnectionsListView connectionsContainer = new ConnectionsListView(connectionsController, indicesController, aliasesController);
        final VBox indexesContainer = new IndexListView(indicesController);
        final VBox aliasesContainer = new AliasListView(aliasesController);

        final SplitPane subViewContainer = new SplitPane(connectionsContainer, indexesContainer, aliasesContainer);
        subViewContainer.setOrientation(Orientation.VERTICAL);
        subViewContainer.setDividerPositions(0.33, 0.66);

        VBox.setVgrow(subViewContainer, Priority.ALWAYS);
        getChildren().add(subViewContainer);

    }

}
