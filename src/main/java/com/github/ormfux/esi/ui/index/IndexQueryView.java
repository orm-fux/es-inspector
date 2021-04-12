package com.github.ormfux.esi.ui.index;

import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.ui.component.JsonTreeView;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class IndexQueryView extends SplitPane {

    private final TextArea queryField = new SourceCodeTextArea();
    
    private final TextArea rawResultField = new TextArea();
    
    private final TreeView<String> treeResultField = new JsonTreeView(); 
    
    public IndexQueryView(final IndexDetailsController indexController) {
        setPadding(new Insets(5));
        
        final SplitPane queryAndMappingView = new SplitPane();
        queryAndMappingView.setOrientation(Orientation.HORIZONTAL);
        queryAndMappingView.setDividerPositions(0.75);
        
        final VBox querySubView = createQueryView(indexController);
        final VBox propertiesView = createPropertiesView(indexController);
        
        queryAndMappingView.getItems().addAll(querySubView, propertiesView);
        
        final TabPane resultSubView = createResultSubView();
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.5);
        getItems().addAll(queryAndMappingView, resultSubView);
    }

    private VBox createQueryView(final IndexDetailsController indexController) {
        final VBox querySubView = new VBox(2);
        querySubView.setPadding(new Insets(2));
        
        final Label queryLabel = new Label("Query");
        queryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(queryLabel, Priority.ALWAYS);
        
        final Button searchButton = new Button("Search");
        searchButton.disableProperty().bind(queryField.textProperty().isEmpty());
        searchButton.setOnAction(e -> {
            final ESSearchResult searchResult = indexController.search(queryField.getText());
            rawResultField.setText(searchResult.getResultString());
            treeResultField.setRoot(searchResult.getFxTree());
        });
        
        final HBox actionsBar = new HBox(2, queryLabel, searchButton);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        
        queryField.setText("{\n  \"from\": 0,\n  \"size\": 10,\n  \"query\": {\n    \"match_all\": {}\n  },\n  \"sort\": [],\n  \"aggs\": {}\n}");
        final ScrollPane queryContainer = new ScrollPane(queryField);
        queryContainer.setFitToHeight(true);
        queryContainer.setFitToWidth(true);
        VBox.setVgrow(queryContainer, Priority.ALWAYS);
        
        querySubView.getChildren().addAll(actionsBar, queryContainer);
        
        return querySubView;
    }
    
    private VBox createPropertiesView(final IndexDetailsController indexController) {
        final VBox propertiesSubView = new VBox(2);
        propertiesSubView.setPadding(new Insets(2));
        
        final ObservableList<String> properties = FXCollections.observableArrayList(indexController.lookupIndexDocumentPropertyPaths());
        
        final ListView<String> propertiesList = new ListView<>(properties);
        propertiesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        propertiesList.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2
                    && propertiesList.getSelectionModel().getSelectedItem() != null) {
                queryField.insertText(queryField.getCaretPosition() >= 0 ? queryField.getCaretPosition() : 0, propertiesList.getSelectionModel().getSelectedItem());
            }
        });
        VBox.setVgrow(propertiesList, Priority.ALWAYS);
        
        final Label propertiesLabel = new Label("Document Properties");
        propertiesLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(propertiesLabel, Priority.ALWAYS);

        final Button addButton = new Button("Add to Query");
        addButton.disableProperty().bind(propertiesList.getSelectionModel().selectedItemProperty().isNull());
        addButton.setOnAction(e -> queryField.insertText(queryField.getCaretPosition() >= 0 ? queryField.getCaretPosition() : 0, propertiesList.getSelectionModel().getSelectedItem()));
        
        final HBox actionsBar = new HBox(2, propertiesLabel, addButton);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        
        final BorderPane filterBar = new BorderPane();
        filterBar.setPadding(new Insets(2));

        final Label filterLabel = new Label("Filter");
        filterBar.setCenter(filterLabel);

        final TextField filterField = new TextField();
        filterField.textProperty().addListener((prop, oldText, newText) -> propertiesList.setItems(properties.filtered(conn -> newText == null || conn.contains(newText))));
        filterBar.setRight(filterField);
        
        propertiesSubView.getChildren().addAll(actionsBar, propertiesList, filterBar);
        
        return propertiesSubView;
    }
    
    private TabPane createResultSubView() {
        final TabPane view = new TabPane();
        view.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        final Tab rawResultTab = new Tab("Raw");
        rawResultField.setFont(Font.font("Courier New"));
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
        view.getSelectionModel().select(treeResultTab);
        
        return view;
    }
}
