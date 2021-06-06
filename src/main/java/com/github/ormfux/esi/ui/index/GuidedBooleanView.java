package com.github.ormfux.esi.ui.index;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.github.ormfux.esi.model.session.GuidedBooleanCondition;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import lombok.Getter;

public class GuidedBooleanView extends GridPane {
    
    private final List<GuidedBooleanQueryCondition> guidedQueryFields = new ArrayList<>(); 
    
    private final TextField guidedFromField = new TextField("0");
    
    private final TextField guidedSizeField = new TextField("10");
    
    private final ObservableList<String> properties;
    
    private final Label pageLabel = new Label("From, Page Size");
    
    private final HBox pageFieldsContainer = new HBox(2);
    
    public GuidedBooleanView(final ObservableList<String> properties) {
        this.properties = properties;
        setHgap(2);
        setVgap(3);
        
        final Button addButton = new Button("+");
        GridPane.setHgrow(addButton, Priority.NEVER);
        addRow(0, addButton);
        
        GridPane.setHgrow(pageLabel, Priority.NEVER);
        GridPane.setHgrow(pageFieldsContainer, Priority.NEVER);
        guidedFromField.setPrefColumnCount(5);
        guidedSizeField.setPrefColumnCount(5);
        pageFieldsContainer.getChildren().addAll(guidedFromField, new Label(", "), guidedSizeField);
        pageFieldsContainer.setAlignment(Pos.CENTER_LEFT);
        addRow(1, pageLabel, pageFieldsContainer);
        
        addButton.setOnAction(e -> {
            createConditionFields();
        });
        
        addButton.fire();
    }

    private GuidedBooleanQueryCondition createConditionFields() {
        final Button removeButton = new Button("x");
        final GuidedBooleanQueryCondition newCondition = new GuidedBooleanQueryCondition(properties, removeButton);
        guidedQueryFields.add(newCondition);
        
        GridPane.setRowIndex(pageLabel, guidedQueryFields.size() + 1);
        GridPane.setRowIndex(pageFieldsContainer, guidedQueryFields.size() + 1);
        
        removeButton.setOnAction(e2 -> {
            getChildren().removeAll(newCondition.getAllNodes());
            
            for (int idx = guidedQueryFields.indexOf(newCondition) + 1; idx < guidedQueryFields.size(); idx++) {
                for (final Node node : guidedQueryFields.get(idx).getAllNodes()) {
                    GridPane.setRowIndex(node, idx);
                }
            }
            
            guidedQueryFields.remove(newCondition);
        });
        
        addRow(guidedQueryFields.size(), 
                       newCondition.getRequired(), 
                       newCondition.getPropertyName(),
                       newCondition.getCondition(),
                       newCondition.getValue(),
                       newCondition.getToLabel(),
                       newCondition.getValueTo(),
                       removeButton);
        
        return newCondition;
    }
    
    public String buildGuidedQuery() {
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
    
    public List<GuidedBooleanCondition> getRestorableGuidedBooleanQuery() {
        final List<GuidedBooleanCondition> conditions = new ArrayList<>();
        
        for (final GuidedBooleanQueryCondition uiCondition : guidedQueryFields) {
            final GuidedBooleanCondition condition = new GuidedBooleanCondition();
            condition.setCondition(uiCondition.getCondition().getSelectionModel().getSelectedItem());
            condition.setPropertyName(uiCondition.getPropertyName().getSelectionModel().getSelectedItem());
            condition.setRequired(uiCondition.getRequired().getSelectionModel().getSelectedItem());
            condition.setValue(uiCondition.getValue().getText());
            condition.setValueTo(uiCondition.getValueTo().getText());
            
            conditions.add(condition);
        }
        
        return conditions;
    }
    
    public void restoreFields(final List<GuidedBooleanCondition> restoredConditions) {
        guidedQueryFields.forEach(condition -> getChildren().removeAll(condition.getAllNodes()));
        
        for (final GuidedBooleanCondition restoredCondition : restoredConditions) {
            final GuidedBooleanQueryCondition conditionFields = createConditionFields();
            conditionFields.getCondition().getSelectionModel().select(restoredCondition.getCondition());
            conditionFields.getPropertyName().getSelectionModel().select(restoredCondition.getPropertyName());
            conditionFields.getRequired().getSelectionModel().select(restoredCondition.getRequired());
            conditionFields.getValue().setText(restoredCondition.getValue());
            conditionFields.getValueTo().setText(restoredCondition.getValueTo());
        }
    }

    @Getter
    private static class GuidedBooleanQueryCondition {
        
        private final ComboBox<String> propertyName;
        
        private final ComboBox<String> condition;
        
        private final ComboBox<String> required;
        
        private final TextField value;
        
        private final Text toLabel = new Text(" - ");
        
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
