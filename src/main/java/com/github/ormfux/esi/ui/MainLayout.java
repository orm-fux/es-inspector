package com.github.ormfux.esi.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class MainLayout extends AnchorPane {

    public MainLayout(final Node connectionsListView, final Node indexDetailsView, final Node loggingView) {
        final SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        mainSplitPane.setDividerPositions(0.8);
        
        final SplitPane subSplitPane = new SplitPane();
        subSplitPane.setOrientation(Orientation.HORIZONTAL);
        subSplitPane.setDividerPositions(0.2);
        
        final StackPane leftContent = new StackPane(connectionsListView);
        final StackPane rightContent = new StackPane(indexDetailsView);
        subSplitPane.getItems().addAll(leftContent, rightContent);
        
        mainSplitPane.getItems().addAll(subSplitPane, loggingView);
        
        AnchorPane.setTopAnchor(mainSplitPane, 2.0);
        AnchorPane.setRightAnchor(mainSplitPane, 2.0);
        AnchorPane.setBottomAnchor(mainSplitPane, 2.0);
        AnchorPane.setLeftAnchor(mainSplitPane, 2.0);
        getChildren().addAll(mainSplitPane);
    }

}
