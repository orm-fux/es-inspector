package com.github.ormfux.esi.controller;

import java.util.Objects;
import java.util.function.Consumer;

import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.service.ESConnectionUsageStatusService;
import com.github.ormfux.esi.service.ManageIndexService;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Setter;

@Bean
public class ManageIndicesController {

    private final ManageIndexService indexService;
    
    private final ObservableList<ESIndex> indexes = FXCollections.observableArrayList();
    
    private final SimpleObjectProperty<ESConnection> selectedConnection = new SimpleObjectProperty<>(); 
    
    @Setter
    private Consumer<ESIndex> detailsViewOpener;
    
    @BeanConstructor
    public ManageIndicesController(final ManageIndexService indexService, final ESConnectionUsageStatusService connectionUsageService) {
        this.indexService = indexService;
        
        selectedConnection.addListener((prop, oldConnection, newConnection) -> {
            if (!Objects.equals(oldConnection, newConnection)) {
                if (oldConnection != null) {
                    connectionUsageService.connectionClosed(oldConnection.getId());
                }
                
                if (newConnection != null) {
                    connectionUsageService.connectionOpened(newConnection.getId());
                }
                
                loadIndices(newConnection);
            }
        });
    }
    
    public void createIndex(final String name, final String indexProperties) {
        final ESConnection connection = selectedConnection.getValue();
        indexService.createIndex(connection, name, indexProperties);
        loadIndices(connection);
    }
    
    public void deleteIndex(final ESIndex index) {
        indexService.deleteIndex(index.getConnection(), index.getName());
        loadIndices(index.getConnection());
    }
    
    private void loadIndices(final ESConnection connection) {
        indexes.clear();
        
        if (connection != null) {
            indexes.addAll(indexService.findAllIndices(connection));
        }
    }
    
    public void openIndexDetails(final ESIndex index) {
        detailsViewOpener.accept(index);
    }
    
    public ObservableList<ESIndex> getAllIndices() {
        return indexes;
    }
    
    public ObjectProperty<ESConnection> getSelectedConnection() {
        return selectedConnection;
    }
    
}
