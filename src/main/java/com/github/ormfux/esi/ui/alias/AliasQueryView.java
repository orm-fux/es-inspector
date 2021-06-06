package com.github.ormfux.esi.ui.alias;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.model.session.SessionAliasDetailsTabData;
import com.github.ormfux.esi.ui.component.AsyncButton;
import com.github.ormfux.esi.ui.component.JsonTableView;
import com.github.ormfux.esi.ui.component.JsonTreeView;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;
import com.github.ormfux.esi.ui.images.ImageButton;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.template.QueryTemplatesMenu;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class AliasQueryView extends SplitPane {

    private final TextArea queryField = new SourceCodeTextArea();
    
    private final TextArea rawResultField = new SourceCodeTextArea();
    
    private final JsonTreeView treeResultField = new JsonTreeView();
    
    private final JsonTableView tableResultField = new JsonTableView(15);
    
    public AliasQueryView(final AliasDetailsController aliasController) {
        setPadding(new Insets(5));
        
        final VBox querySubView = createQueryView(aliasController);
        
        final AnchorPane resultSubView = createResultSubView();
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.5);
        getItems().addAll(querySubView, resultSubView);
    }

    private VBox createQueryView(final AliasDetailsController aliasController) {
        final VBox querySubView = new VBox(2);
        querySubView.setPadding(new Insets(2));
        
        final Label queryLabel = new Label("");
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
        
        final MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(new QueryTemplatesMenu(aliasController, queryField.textProperty(), this));
        
        final HBox actionsBar = new HBox(2, queryLabel, searchButton, menuBar, runningIcon);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        
        queryField.setText("{\n  \"from\": 0,\n  \"size\": 10,\n  \"query\": {\n    \"match_all\": {}\n  },\n  \"sort\": [],\n  \"aggs\": {}\n}");
        final ScrollPane queryContainer = new ScrollPane(queryField);
        queryContainer.setFitToHeight(true);
        queryContainer.setFitToWidth(true);
        VBox.setVgrow(queryContainer, Priority.ALWAYS);
        
        querySubView.getChildren().addAll(actionsBar, queryContainer);
        
        return querySubView;
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

    protected void fillWithData(final SessionAliasDetailsTabData restoreData) {
        queryField.setText(restoreData.getPlainQuery());
    }
}
