package com.github.ormfux.esi.ui.alias;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.ui.component.AsyncButton;
import com.github.ormfux.esi.ui.component.JsonTableView;
import com.github.ormfux.esi.ui.component.JsonTreeView;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AliasQueryView extends SplitPane {

    private final TextArea queryField = new SourceCodeTextArea();
    
    private final TextArea rawResultField = new SourceCodeTextArea();
    
    private final JsonTreeView treeResultField = new JsonTreeView();
    
    private final JsonTableView tableResultField = new JsonTableView();
    
    public AliasQueryView(final AliasDetailsController aliasController) {
        setPadding(new Insets(5));
        
        final VBox querySubView = createQueryView(aliasController);
        
        final TabPane resultSubView = createResultSubView();
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.5);
        getItems().addAll(querySubView, resultSubView);
    }

    private VBox createQueryView(final AliasDetailsController aliasController) {
        final VBox querySubView = new VBox(2);
        querySubView.setPadding(new Insets(2));
        
        final Label queryLabel = new Label("Query");
        queryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(queryLabel, Priority.ALWAYS);
        
        final AsyncButton searchButton = new AsyncButton("Search");
        final Node runningIcon = searchButton.getRunningIndicator();
        searchButton.disableProperty().bind(queryField.textProperty().isEmpty().or(runningIcon.visibleProperty()));
        searchButton.setAction(() -> {
            final ESSearchResult searchResult = aliasController.search(queryField.getText());
            rawResultField.setText(searchResult.getResultString());
            treeResultField.setTree(searchResult.getFxTree());
            Platform.runLater(() ->  tableResultField.setTableContent(searchResult.getTableData()));
        });
        final HBox actionsBar = new HBox(2, queryLabel, searchButton, runningIcon);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        
        queryField.setText("{\n  \"from\": 0,\n  \"size\": 10,\n  \"query\": {\n    \"match_all\": {}\n  },\n  \"sort\": [],\n  \"aggs\": {}\n}");
        final ScrollPane queryContainer = new ScrollPane(queryField);
        queryContainer.setFitToHeight(true);
        queryContainer.setFitToWidth(true);
        VBox.setVgrow(queryContainer, Priority.ALWAYS);
        
        querySubView.getChildren().addAll(actionsBar, queryContainer);
        
        return querySubView;
    }
    
    
    private TabPane createResultSubView() {
        final TabPane view = new TabPane();
        view.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        final Tab treeResultTab = new Tab("Tree");

        final ScrollPane treeScroll = new ScrollPane(treeResultField);
        treeScroll.setFitToHeight(true);
        treeScroll.setFitToWidth(true);
        treeResultTab.setContent(treeScroll);
        
        view.getTabs().add(treeResultTab);
        view.getSelectionModel().select(treeResultTab);
        
        final Tab tableResultTab = new Tab("Table");

        final ScrollPane tableScroll = new ScrollPane(tableResultField);
        tableScroll.setFitToHeight(true);
        tableScroll.setFitToWidth(true);
        tableResultTab.setContent(tableScroll);
        
        view.getTabs().add(tableResultTab);
        
        final Tab rawResultTab = new Tab("Raw");
        rawResultField.setEditable(false);
        final ScrollPane rawScroll = new ScrollPane(rawResultField);
        rawScroll.setFitToHeight(true);
        rawScroll.setFitToWidth(true);
        rawResultTab.setContent(rawScroll);
        
        view.getTabs().add(rawResultTab);
        
        return view;
    }
}
