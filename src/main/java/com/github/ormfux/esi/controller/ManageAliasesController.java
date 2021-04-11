package com.github.ormfux.esi.controller;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.ormfux.esi.model.alias.ESMultiIndexAlias;
import com.github.ormfux.esi.model.alias.ESSingleIndexAlias;
import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.service.ESConnectionUsageStatusService;
import com.github.ormfux.esi.service.ManageAliasService;
import com.github.ormfux.esi.service.ManageIndexService;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Setter;

@Bean
public class ManageAliasesController {

    private final ManageAliasService aliasService;
    
    private final ManageIndexService indicesService;
    
    private final ObservableList<ESMultiIndexAlias> aliases = FXCollections.observableArrayList();
    
    private final SimpleObjectProperty<ESConnection> selectedConnection = new SimpleObjectProperty<>(); 
    
    @Setter
    private Consumer<ESMultiIndexAlias> detailsViewOpener;
    
    @BeanConstructor
    public ManageAliasesController(final ManageAliasService aliasService,
                                   final ManageIndexService indicesService,
                                   final ESConnectionUsageStatusService connectionUsageService) {
        this.aliasService = aliasService;
        this.indicesService = indicesService;
        
        selectedConnection.addListener((prop, oldConnection, newConnection) -> {
            if (!Objects.equals(oldConnection, newConnection)) {
                if (oldConnection != null) {
                    connectionUsageService.connectionClosed(oldConnection.getId());
                }
                
                if (newConnection != null) {
                    connectionUsageService.connectionOpened(newConnection.getId());
                }
                
                loadAliases(newConnection);
            }
        });
    }
    
    public List<String> loadAllIndexNames() {
        return indicesService.findAllIndices(selectedConnection.get()).stream().map(ESIndex::getName).collect(toList());
    }
    
    public void createAlias(final ESSingleIndexAlias alias) {
        final ESConnection connection = selectedConnection.getValue();
        aliasService.createAlias(connection, alias);
        loadAliases(connection);
    }
    
    public void deleteAlias(final String aliasName, final String indexName) {
        aliasService.deleteAlias(selectedConnection.getValue(), aliasName, indexName);
        loadAliases(selectedConnection.getValue());
    }
    
    private void loadAliases(final ESConnection connection) {
        aliases.clear();
        
        if (connection != null) {
            aliases.addAll(aliasService.findAllAliases(connection));
        }
    }
    
    public void openAliasDetails(final ESMultiIndexAlias alias) {
        detailsViewOpener.accept(alias);
    }
    
    public ObservableList<ESMultiIndexAlias> getAllAliases() {
        return aliases;
    }
    
    public ObjectProperty<ESConnection> getSelectedConnection() {
        return selectedConnection;
    }
    
}
