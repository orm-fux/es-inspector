package com.github.ormfux.esi.ui.template;

import java.util.List;
import java.util.Optional;

import com.github.ormfux.esi.controller.AbstractQueryTemplateController;
import com.github.ormfux.esi.model.template.QueryTemplate;
import com.github.ormfux.esi.ui.component.ActionButtonTableCell;
import com.github.ormfux.esi.ui.images.ImageKey;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ManageTemplatesDialog extends Stage {
    
    private final ComboBox<String> templateTypeBox = new ComboBox<>(FXCollections.observableArrayList("Connection", "Global"));
    
    private final TableView<QueryTemplate> templatesTable;
    
    public ManageTemplatesDialog(final Node owner, final AbstractQueryTemplateController templatesController) {
        setTitle("Manage Query Templates");
        setResizable(true);
        setHeight(300);
        setWidth(550);
        initOwner(owner.getScene().getWindow());
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UTILITY);
        
        final VBox content = new VBox(2);
        content.setPadding(new Insets(3));
        
        final HBox typeSelectionContainer = new HBox(2);
        typeSelectionContainer.getChildren().addAll(new Label("Type"), templateTypeBox);
        typeSelectionContainer.setAlignment(Pos.CENTER_LEFT);
        templateTypeBox.getSelectionModel().select("Connection");
        
        templateTypeBox.getSelectionModel().selectedItemProperty().addListener((prop, oldType, newType) -> refreshDisplayedTemplates("Global".equals(newType), templatesController));
        
        templatesTable = createTemplatesTableView(templatesController);
        
        content.getChildren().addAll(typeSelectionContainer, templatesTable);
        
        setOnShown(e -> refreshDisplayedTemplates(templatesController));
        setScene(new Scene(content));
    }

    @SuppressWarnings("unchecked")
    private TableView<QueryTemplate> createTemplatesTableView(final AbstractQueryTemplateController templatesController) {
        final TableView<QueryTemplate> templatesTable = new TableView<>();
        
        final TableColumn<QueryTemplate, Button> deleteColumn = new TableColumn<>();
        deleteColumn.setCellFactory(f -> new ActionButtonTableCell<QueryTemplate>(ImageKey.DELETE, template -> {
            final Alert alert = new Alert(AlertType.NONE, "Really delete the template '" + template.getName() + "'?", ButtonType.CANCEL, ButtonType.OK);
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                templatesController.deleteQueryTemplate(isGlobalTemplatesDisplayed(), template);
                refreshDisplayedTemplates(templatesController);
            }
            
            return null;
        }));
        deleteColumn.setStyle( "-fx-alignment: CENTER;");
        deleteColumn.setMinWidth(40);
        deleteColumn.setMaxWidth(40);
        deleteColumn.setSortable(false);
        
        final TableColumn<QueryTemplate, Button> editColumn = new TableColumn<>();
        editColumn.setCellFactory(f -> new ActionButtonTableCell<>(ImageKey.EDIT, template -> {
            Optional<QueryTemplate> editedTemplate = new EditQueryTemplateDialog(template).showAndWait();
            
            if (editedTemplate.isPresent()) {
                templatesController.updateQueryTemplate(isGlobalTemplatesDisplayed(), editedTemplate.get());
                refreshDisplayedTemplates(templatesController);
            }
            
            return null;
        }));
        editColumn.setStyle( "-fx-alignment: CENTER;");
        editColumn.setMinWidth(40);
        editColumn.setMaxWidth(40);
        editColumn.setSortable(false);
        
        final TableColumn<QueryTemplate, String> nameColumn = new TableColumn<>("Template");
        nameColumn.setSortable(false);
        nameColumn.setPrefWidth(120);
        nameColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
        nameColumn.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().getName()));
        
        final TableColumn<QueryTemplate, String> queryColumn = new TableColumn<>("Query");
        queryColumn.setSortable(false);
        queryColumn.setStyle( "-fx-alignment: CENTER-LEFT;");
        queryColumn.prefWidthProperty().bind(templatesTable.widthProperty().subtract(210));
        queryColumn.setCellValueFactory(features -> new SimpleStringProperty(features.getValue().getQuery().replaceAll("\n|\r", "")));
        
        templatesTable.getColumns().addAll(deleteColumn, editColumn, nameColumn, queryColumn);
        
        return templatesTable;
    }

    private void refreshDisplayedTemplates(final AbstractQueryTemplateController templatesController) {
        refreshDisplayedTemplates(isGlobalTemplatesDisplayed(), templatesController);
    }
    
    private void refreshDisplayedTemplates(final boolean showGlobal, final AbstractQueryTemplateController templatesController) {
        final List<QueryTemplate> allTemplates;
        
        if (showGlobal) {
            allTemplates = templatesController.lookupGlobalQueryTemplates();
        } else {
            allTemplates = templatesController.lookupConnectionQueryTemplates();
        }
        
        templatesTable.getItems().clear();
        templatesTable.getItems().setAll(allTemplates);
    }
    
    private boolean isGlobalTemplatesDisplayed() {
        return "Global".equals(templateTypeBox.getSelectionModel().getSelectedItem());
    }
    
}
