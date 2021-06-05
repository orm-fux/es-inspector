package com.github.ormfux.esi.ui.template;

import java.util.Optional;

import com.github.ormfux.esi.controller.AbstractQueryTemplateController;
import com.github.ormfux.esi.model.template.QueryTemplate;
import com.github.ormfux.esi.ui.template.CreateQueryTemplateDialog.TemplateCreateData;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class QueryTemplatesMenu extends Menu {
    
    public QueryTemplatesMenu(final AbstractQueryTemplateController templatesController, final StringProperty queryText, final Node owner) {
        setText("Query Templates");
        
        final Menu globalTemplatesMenu = new Menu("Global Templates");
        getItems().add(globalTemplatesMenu);
        final Menu connectionTemplatesMenu = new Menu("Connection Templates");
        getItems().add(connectionTemplatesMenu);
        
        final MenuItem saveQueryAsTemplateItem = new MenuItem("Save Query as Template ...");
        saveQueryAsTemplateItem.setOnAction(e -> {
            final Optional<TemplateCreateData> createData = new CreateQueryTemplateDialog().showAndWait();
            
            if (createData.isPresent()) {
                templatesController.createQueryTemplate(createData.get().getName(), createData.get().isGlobal(), queryText.get());
            }
        });
        
        getItems().add(saveQueryAsTemplateItem);
        
        final MenuItem manageTemplatesItem = new MenuItem("Manage Templates ...");
        manageTemplatesItem.setOnAction(e -> new ManageTemplatesDialog(owner, templatesController).show());
        getItems().add(manageTemplatesItem);
        
        setOnShown(e -> {
            Platform.runLater(() -> {
                globalTemplatesMenu.getItems().clear();
                connectionTemplatesMenu.getItems().clear();
                
                for (final QueryTemplate queryTemplate : templatesController.lookupGlobalQueryTemplates()) {
                    final MenuItem templateItem = new MenuItem(queryTemplate.getName());
                    templateItem.setOnAction(ie -> queryText.set(queryTemplate.getQuery()));
                    globalTemplatesMenu.getItems().add(templateItem);
                }
                
                for (final QueryTemplate queryTemplate : templatesController.lookupConnectionQueryTemplates()) {
                    final MenuItem templateItem = new MenuItem(queryTemplate.getName());
                    templateItem.setOnAction(ie -> queryText.set(queryTemplate.getQuery()));
                    connectionTemplatesMenu.getItems().add(templateItem);
                }
            });
        });
    }
    
}
