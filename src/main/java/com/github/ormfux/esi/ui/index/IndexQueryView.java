package com.github.ormfux.esi.ui.index;

import static javafx.collections.FXCollections.observableArrayList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.model.session.GuidedBooleanCondition;
import com.github.ormfux.esi.model.session.QueryType;
import com.github.ormfux.esi.model.session.SessionIndexDetailsTabData;
import com.github.ormfux.esi.ui.component.AsyncButton;
import com.github.ormfux.esi.ui.component.JsonTableView;
import com.github.ormfux.esi.ui.component.JsonTreeView;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;
import com.github.ormfux.esi.ui.images.ImageButton;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.template.QueryTemplatesMenu;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class IndexQueryView extends SplitPane {

    private final ComboBox<String> queryTypeField = new ComboBox<>(FXCollections.observableArrayList("Plain", "Guided Boolean"));
    
    private final SplitPane plainQuerySubView = new SplitPane();
    
    private final VBox guidedQuerySubView = new VBox();
    
    private GuidedBooleanView guidedQueryFieldsContainer;
    
    private final TextArea queryField = new SourceCodeTextArea();
    
    private final TextArea rawResultField = new SourceCodeTextArea();
    
    private final JsonTreeView treeResultField = new JsonTreeView(); 
    
    private final JsonTableView tableResultField = new JsonTableView(15);
    
    public IndexQueryView(final IndexDetailsController indexController) {
        setPadding(new Insets(5));
        
        final BorderPane querySubView = createQuerySubView(indexController);
        
        final AnchorPane resultSubView = createResultSubView();
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.5);
        getItems().addAll(querySubView, resultSubView);
    }

    private BorderPane createQuerySubView(final IndexDetailsController indexController) {
        final BorderPane querySubView = new BorderPane();
        querySubView.setPadding(new Insets(2));
        
        final AsyncButton searchButton = new AsyncButton("Search");
        final Node runningIcon = searchButton.getRunningIndicator();
        searchButton.disableProperty().bind(queryField.textProperty().isEmpty().or(runningIcon.visibleProperty()));
        searchButton.setAction(() -> {
            final ESSearchResult searchResult;
            
            switch (queryTypeField.getSelectionModel().getSelectedItem()) {
                case "Plain":
                    searchResult = indexController.search(queryField.getText());
                    break;
                case "Guided Boolean":
                    final String guidedQuery = guidedQueryFieldsContainer.buildGuidedQuery();
                    searchResult = indexController.search(guidedQuery);
                    break;
                default:
                    searchResult = null;
            }
            
            rawResultField.setText(searchResult.getResultString());
            treeResultField.setTree(searchResult.getFxTree());
            Platform.runLater(() ->  tableResultField.setTableContent(searchResult.getTableData()));
        });
        
        final MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(new QueryTemplatesMenu(indexController, queryField.textProperty(), this));
        
        final HBox actionsBar = new HBox(2, queryTypeField, searchButton, menuBar, runningIcon);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        actionsBar.setPadding(new Insets(2));
        
        querySubView.setTop(actionsBar);
        
        final ObservableList<String> properties = observableArrayList(indexController.lookupIndexDocumentPropertyPaths());
        
        createPlainQuerySubView(properties);
        createGuidedQuerySubView(properties);
        
        queryTypeField.getSelectionModel().select("Plain");
        
        queryTypeField.getSelectionModel().selectedItemProperty().addListener((prop, oldView, newView) -> {
            if (newView != null) {
                switch (newView) {
                    case "Plain":
                        querySubView.setCenter(plainQuerySubView);
                        menuBar.setVisible(true);
                        menuBar.setManaged(true);
                        break;
                    case "Guided Boolean":
                        querySubView.setCenter(guidedQuerySubView);
                        menuBar.setVisible(false);
                        menuBar.setManaged(false);
                        break;
                }
            }
        });
        
        querySubView.setCenter(plainQuerySubView);
        
        return querySubView;
    }

    private void createPlainQuerySubView(final ObservableList<String> properties) {
        plainQuerySubView.setOrientation(Orientation.HORIZONTAL);
        plainQuerySubView.setDividerPositions(0.75);
        final VBox plainQueryField = createPlainQueryField();
        final VBox propertiesView = createPropertiesView(properties);
        plainQuerySubView.getItems().addAll(plainQueryField, propertiesView);
    }

    private VBox createPlainQueryField() {
        final VBox querySubView = new VBox(2);
        querySubView.setPadding(new Insets(2));
        
        queryField.setText("{\n  \"from\": 0,\n  \"size\": 10,\n  \"query\": {\n    \"match_all\": {}\n  },\n  \"sort\": [],\n  \"aggs\": {}\n}");
        final ScrollPane queryContainer = new ScrollPane(queryField);
        queryContainer.setFitToHeight(true);
        queryContainer.setFitToWidth(true);
        VBox.setVgrow(queryContainer, Priority.ALWAYS);
        
        querySubView.getChildren().addAll(queryContainer);
        
        return querySubView;
    }
    
    private VBox createPropertiesView(final ObservableList<String> properties) {
        final VBox propertiesSubView = new VBox(2);
        propertiesSubView.setPadding(new Insets(2));
        
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
    
    private void createGuidedQuerySubView(final ObservableList<String> properties) {
        guidedQueryFieldsContainer = new GuidedBooleanView(properties);
        
        final ScrollPane scroll = new ScrollPane(guidedQueryFieldsContainer);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(2));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        guidedQuerySubView.getChildren().add(scroll);
    }
    
    private AnchorPane createResultSubView() {
        final TabPane tabView = new TabPane();
        tabView.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        final Tab treeResultTab = new Tab("Tree");

        final ScrollPane treeScroll = new ScrollPane(treeResultField);
        treeScroll.setFitToHeight(true);
        treeScroll.setFitToWidth(true);
        treeResultTab.setContent(treeScroll);
        
        tabView.getTabs().add(treeResultTab);
        tabView.getSelectionModel().select(treeResultTab);
        
        final Tab tableResultTab = new Tab("Table");

        final ScrollPane tableScroll = new ScrollPane(tableResultField);
        tableScroll.setFitToHeight(true);
        tableScroll.setFitToWidth(true);
        tableResultTab.setContent(tableScroll);
        
        tabView.getTabs().add(tableResultTab);
        
        final Tab rawResultTab = new Tab("Raw");
        rawResultField.setEditable(false);
        final ScrollPane rawScroll = new ScrollPane(rawResultField);
        rawScroll.setFitToHeight(true);
        rawScroll.setFitToWidth(true);
        rawResultTab.setContent(rawScroll);
        
        tabView.getTabs().add(rawResultTab);
        
        final HBox actionBar = new HBox();
        
        final ImageButton saveButton = new ImageButton(ImageKey.SAVE);
        saveButton.disableProperty().bind(rawResultField.textProperty().isEmpty());
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JSON files (*.json)", "*.json", "*.JSON"), new ExtensionFilter("CSV files (*.csv)", "*.csv", "*.CSV"));
        
        saveButton.setOnAction(e -> {
            final File targetFile = fileChooser.showSaveDialog(getScene().getWindow());
            
            if (targetFile != null) {
                final String fileName = targetFile.getName();
                
                try {
                    if (fileName.endsWith(".csv") || fileName.endsWith(".CSV")) {
                        Files.writeString(targetFile.toPath(), tableResultField.toCsv());
                    } else {
                        Files.writeString(targetFile.toPath(), rawResultField.getText());
                    }
                } catch (final IOException ex) {
                    throw new ApplicationException("Error saving to file " + targetFile, ex);
                }
            }
            
        });
        
        actionBar.getChildren().addAll(saveButton);

        final AnchorPane resultView = new AnchorPane();
        resultView.getChildren().addAll(tabView, actionBar);
        AnchorPane.setTopAnchor(actionBar, 3.0);
        AnchorPane.setRightAnchor(actionBar, 5.0);
        AnchorPane.setTopAnchor(tabView, 1.0);
        AnchorPane.setRightAnchor(tabView, 1.0);
        AnchorPane.setLeftAnchor(tabView, 1.0);
        AnchorPane.setBottomAnchor(tabView, 1.0);
        
        return resultView;
    }
    
    protected String getPlainQuery() {
        return queryField.getText();
    }

    protected QueryType getSelectedQueryType() {
        if ("Plain".equals(queryTypeField.getSelectionModel().getSelectedItem())) {
            return QueryType.PLAIN;
        } else {
            return QueryType.GUIDED_BOOLEAN;
        }
    }

    protected List<GuidedBooleanCondition> getGuidedBooleanQuery() {
        return guidedQueryFieldsContainer.getRestorableGuidedBooleanQuery();
    }

    protected void fillWithData(final SessionIndexDetailsTabData viewData) {
        queryField.setText(viewData.getPlainQuery());
        
        guidedQueryFieldsContainer.restoreFields(viewData.getGuidedBooleanQuery());
        
        switch (viewData.getSelectedQueryType()) {
            case GUIDED_BOOLEAN:
                queryTypeField.getSelectionModel().select("Guided Boolean");
                break;
            case PLAIN:
                queryTypeField.getSelectionModel().select("Plain");
                break;
        }
    }
}
