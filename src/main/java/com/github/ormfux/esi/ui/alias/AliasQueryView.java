package com.github.ormfux.esi.ui.alias;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AliasQueryView extends SplitPane {

    private final TextArea queryField = new SourceCodeTextArea();
    
    private final TextArea resultField = new TextArea();
    
    public AliasQueryView(final AliasDetailsController aliasController) {
        setPadding(new Insets(5));
        
        final VBox querySubView = createQueryView(aliasController);
        
        resultField.setFont(Font.font("Courier New"));
        resultField.setEditable(false);
        final ScrollPane resultContainer = new ScrollPane(resultField);
        resultContainer.setFitToHeight(true);
        resultContainer.setFitToWidth(true);
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.5);
        getItems().addAll(new StackPane(querySubView), new StackPane(resultContainer));
    }

    private VBox createQueryView(final AliasDetailsController aliasController) {
        final VBox querySubView = new VBox(2);
        querySubView.setPadding(new Insets(2));
        
        final Label queryLabel = new Label("Query");
        queryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(queryLabel, Priority.ALWAYS);
        
        final Button searchButton = new Button("Search");
        searchButton.disableProperty().bind(queryField.textProperty().isEmpty());
        searchButton.setOnAction(e -> resultField.setText(aliasController.search(queryField.getText())));
        
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
}
