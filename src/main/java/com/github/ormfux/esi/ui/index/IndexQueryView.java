package com.github.ormfux.esi.ui.index;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.ui.component.AsyncButton;
import com.github.ormfux.esi.ui.component.JsonTreeView;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;

public class IndexQueryView extends SplitPane {

    private final ComboBox<String> queryTypeField = new ComboBox<>(FXCollections.observableArrayList("Plain", "Guided Boolean"));
    
    private final SplitPane plainQuerySubView = new SplitPane();
    
    private final VBox guidedQuerySubView = new VBox();
    
    private final TextArea queryField = new SourceCodeTextArea();
    
    private final List<GuidedBooleanQueryCondition> guidedQueryFields = new ArrayList<>(); 
    
    final TextField guidedFromField = new TextField("0");
    
    final TextField guidedSizeField = new TextField("10");
    
    private final TextArea rawResultField = new SourceCodeTextArea();
    
    private final JsonTreeView treeResultField = new JsonTreeView(); 
    
    public IndexQueryView(final IndexDetailsController indexController) {
        setPadding(new Insets(5));
        
        final BorderPane querySubView = createQuerySubView(indexController);
        
        final TabPane resultSubView = createResultSubView();
        
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0.5);
        getItems().addAll(querySubView, resultSubView);
    }

    private BorderPane createQuerySubView(final IndexDetailsController indexController) {
        final BorderPane querySubView = new BorderPane();
        querySubView.setPadding(new Insets(2));
        
        final Label queryTypeLabel = new Label("Query Type");
        queryTypeLabel.setMaxWidth(Double.MAX_VALUE);
        
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
                    final String guidedQuery = buildGuidedQuery();
                    searchResult = indexController.search(guidedQuery);
                    break;
                default:
                    searchResult = null;
            }
            
            rawResultField.setText(searchResult.getResultString());
            treeResultField.setTree(searchResult.getFxTree());
        });
        
        final HBox actionsBar = new HBox(2, queryTypeLabel, queryTypeField, searchButton, runningIcon);
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
                        break;
                    case "Guided Boolean":
                        querySubView.setCenter(guidedQuerySubView);
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
        final GridPane content = new GridPane();
        content.setHgap(2);
        content.setVgap(3);
        
        final Button addButton = new Button("+");
        GridPane.setHgrow(addButton, Priority.NEVER);
        content.addRow(0, addButton);
        
        final Label pageLabel = new Label("From, Page Size");
        GridPane.setHgrow(pageLabel, Priority.NEVER);
        final HBox pageFieldsContainer = new HBox(2);
        GridPane.setHgrow(pageFieldsContainer, Priority.NEVER);
        guidedFromField.setPrefColumnCount(5);
        guidedSizeField.setPrefColumnCount(5);
        pageFieldsContainer.getChildren().addAll(guidedFromField, new Label(" - "), guidedSizeField);
        pageFieldsContainer.setAlignment(Pos.CENTER_LEFT);
        content.addRow(1, pageLabel, pageFieldsContainer);
        
        addButton.setOnAction(e -> {
            final Button removeButton = new Button("x");
            final GuidedBooleanQueryCondition newCondition = new GuidedBooleanQueryCondition(properties, removeButton);
            guidedQueryFields.add(newCondition);
            
            GridPane.setRowIndex(pageLabel, guidedQueryFields.size() + 1);
            GridPane.setRowIndex(pageFieldsContainer, guidedQueryFields.size() + 1);
            
            removeButton.setOnAction(e2 -> {
                content.getChildren().removeAll(newCondition.getAllNodes());
                
                for (int idx = guidedQueryFields.indexOf(newCondition) + 1; idx < guidedQueryFields.size(); idx++) {
                    for (final Node node : guidedQueryFields.get(idx).getAllNodes()) {
                        GridPane.setRowIndex(node, idx);
                    }
                }
                
                guidedQueryFields.remove(newCondition);
            });
            
            content.addRow(guidedQueryFields.size(), 
                           newCondition.getRequired(), 
                           newCondition.getPropertyName(),
                           newCondition.getCondition(),
                           newCondition.getValue(),
                           newCondition.getToLabel(),
                           newCondition.getValueTo(),
                           removeButton);
            
        });
        
        addButton.fire();
        
        final ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        scroll.setPadding(new Insets(2));
        
        guidedQuerySubView.getChildren().add(scroll);
    }
    
    
    private TabPane createResultSubView() {
        final TabPane view = new TabPane();
        view.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        final Tab rawResultTab = new Tab("Raw");
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
    
    private String buildGuidedQuery() {
        final StringBuilder query = new StringBuilder();
        query.append("{ \"from\": ").append(guidedFromField.getText())
             .append(", \"size\": ").append(guidedSizeField.getText())
             .append(", \"query\": { \"bool\": { ");
        
        final StringJoiner mustConditions = new StringJoiner(", ", "[", "]");
        mustConditions.setEmptyValue("{}");
        
        guidedQueryFields.stream()
                         .filter(field -> "must".equals(field.getRequired().getSelectionModel().getSelectedItem()))
                         .filter(field -> field.getPropertyName().getSelectionModel().getSelectedItem() != null)
                         .map(this::mapGuidedCondition)
                         .forEach(mustConditions::add);
        
        final StringJoiner mustNotConditions = new StringJoiner(", ", "[", "]");
        mustNotConditions.setEmptyValue("{}");
        
        guidedQueryFields.stream()
                         .filter(field -> "must_not".equals(field.getRequired().getSelectionModel().getSelectedItem()))
                         .filter(field -> field.getPropertyName().getSelectionModel().getSelectedItem() != null)
                         .map(this::mapGuidedCondition)
                         .forEach(mustNotConditions::add);
        
        final StringJoiner shouldConditions = new StringJoiner(", ", "[", "]");
        shouldConditions.setEmptyValue("{}");
        
        guidedQueryFields.stream()
                         .filter(field -> "should".equals(field.getRequired().getSelectionModel().getSelectedItem()))
                         .filter(field -> field.getPropertyName().getSelectionModel().getSelectedItem() != null)
                         .map(this::mapGuidedCondition)
                         .forEach(shouldConditions::add);
        
        final StringJoiner conditionGroupJoiner = new StringJoiner(", ");
        
        if (mustConditions.length() != 2) {
            conditionGroupJoiner.add("\"must\": " + mustConditions);
        }
        
        if (mustNotConditions.length() != 2) {
            conditionGroupJoiner.add("\"must_not\": " + mustNotConditions);
        }
        
        if (shouldConditions.length() != 2) {
            conditionGroupJoiner.add("\"should\": " + shouldConditions);
        }
        
        query.append(conditionGroupJoiner);
        
        query.append("} } }");
        
        return query.toString();
    }
    
    private String mapGuidedCondition(final GuidedBooleanQueryCondition condition) {
        final StringBuilder mappedCondition = new StringBuilder("{");
        
        switch (condition.getCondition().getSelectionModel().getSelectedItem()) {
            case "match":
                mappedCondition.append("\"match\": {")
                               .append("\"").append(condition.getPropertyName().getSelectionModel().getSelectedItem()).append("\": ")
                               .append("\"").append(condition.getValue().getText()).append("\"")
                               .append("}");
                break;
            case "term":
                mappedCondition.append("\"term\": {")
                               .append("\"").append(condition.getPropertyName().getSelectionModel().getSelectedItem()).append("\": ")
                               .append("\"").append(condition.getValue().getText()).append("\"")
                               .append("}");
                break;
            case "range":
                mappedCondition.append("\"range\": {")
                               .append("\"").append(condition.getPropertyName().getSelectionModel().getSelectedItem()).append("\": ")
                               .append("{ \"gte\": ").append(condition.getValue().getText()).append(", \"lte\": ").append(condition.getValueTo().getText()).append("}")
                               .append("}");
                break;
            case "exists":
                mappedCondition.append("\"exists\": {")
                               .append("\"field\": ")
                               .append("\"").append(condition.getPropertyName().getSelectionModel().getSelectedItem()).append("\"")
                               .append("}");
                break;
        }
        
        mappedCondition.append("}");
        
        return mappedCondition.toString();
    }
    
    @Getter
    private static class GuidedBooleanQueryCondition {
        
        private final ComboBox<String> propertyName;
        
        private final ComboBox<String> condition;
        
        private final ComboBox<String> required;
        
        private final TextField value;
        
        private final Text toLabel = new Text(", ");
        
        private final TextField valueTo;
        
        private final Button removeButton;
        
        public GuidedBooleanQueryCondition(final ObservableList<String> selectableProperties, final Button removeButton) {
            this.removeButton = removeButton;
            propertyName = new ComboBox<>(selectableProperties);
            
            condition = new ComboBox<>(observableArrayList("match", "term", "range", "exists"));
            condition.getSelectionModel().select("match");
            
            required = new ComboBox<>(observableArrayList("must", "must_not", "should"));
            required.getSelectionModel().select("must");
            
            value = new TextField();
            valueTo = new TextField();
            
            value.visibleProperty().bind(condition.getSelectionModel().selectedItemProperty().isNotEqualTo("exists"));
            value.managedProperty().bind(condition.getSelectionModel().selectedItemProperty().isNotEqualTo("exists"));
            
            valueTo.visibleProperty().bind(condition.getSelectionModel().selectedItemProperty().isEqualTo("range"));
            valueTo.managedProperty().bind(condition.getSelectionModel().selectedItemProperty().isEqualTo("range"));
            toLabel.visibleProperty().bind(condition.getSelectionModel().selectedItemProperty().isEqualTo("range"));
            toLabel.managedProperty().bind(condition.getSelectionModel().selectedItemProperty().isEqualTo("range"));
        }
        
        public List<Node> getAllNodes() {
            return List.of(propertyName, condition, required, value, toLabel, valueTo, removeButton);
        }
    }
}
