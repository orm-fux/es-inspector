package com.github.ormfux.esi.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.service.ESConnectionUsageStatusService;
import com.github.ormfux.esi.service.ESRestClient;
import com.github.ormfux.esi.service.JsonService;
import com.github.ormfux.esi.service.QueryTemplateService;
import com.github.ormfux.esi.util.Constants;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Setter;

@Bean
public class ManageConnectionsController {
    
    private final JsonService jsonService;
    
    private final QueryTemplateService queryTemplateService;
    
    private final ESConnectionUsageStatusService connectionStatusService;
    
    private final ESRestClient restClient;
    
    private final ObservableList<ESConnection> connections = FXCollections.observableArrayList();
    
    @Setter
    private Consumer<ESConnection> godModeViewOpener;
    
    private final List<Consumer<ESConnection>> closeConnectionHandlers = new ArrayList<>();
    
    @BeanConstructor
    public ManageConnectionsController(final JsonService jsonService, 
                                       final ESRestClient restClient, 
                                       final ESConnectionUsageStatusService connectionStatusService,
                                       final QueryTemplateService queryTemplateService) {
        this.jsonService = jsonService;
        this.restClient = restClient;
        this.connectionStatusService = connectionStatusService;
        this.queryTemplateService = queryTemplateService;
        
        if (!Constants.CONNECTION_SETTINGS_DIR.exists()) {
            Constants.CONNECTION_SETTINGS_DIR.mkdirs();
        }
        
        loadConnections();
    }
    
    public ObservableList<ESConnection> getAllConnections() {
        return connections;
    }
    
    public boolean isUsed(final ESConnection connection) {
        return connectionStatusService.isConnected(connection.getId());
    }
    
    public String lookupElasticsearchVersion(final ESConnection connection) {
        return restClient.ping(connection).orElse(null);
    }
    
    public void createConnection(final ESConnection newConnection) {
        final String id = UUID.randomUUID().toString();
        newConnection.setId(id);
        jsonService.writeToFile(new File(Constants.CONNECTION_SETTINGS_DIR, id +".json"), newConnection);
        loadConnections();
    }
    
    public void deleteConnection(final ESConnection deletedConnection) {
        new File(Constants.CONNECTION_SETTINGS_DIR, deletedConnection.getId() +".json").delete();
        queryTemplateService.deleteTemplateCollection(deletedConnection.getId());
        loadConnections();
    }
    
    public void editConnection(final ESConnection editedConnection) {
        new File(Constants.CONNECTION_SETTINGS_DIR, editedConnection.getId() +".json").delete();
        createConnection(editedConnection);
    }
    
    public void openGodModeView(final ESConnection connection) {
        godModeViewOpener.accept(connection);
    }
    
    public void closeConnection(final ESConnection connection) {
        closeConnectionHandlers.stream().forEach(handler -> handler.accept(connection));
    }
    
    public void addCloseConnectionHandler(final Consumer<ESConnection> handler) {
        closeConnectionHandlers.add(handler);
    }
    
    private void loadConnections() {
        List<ESConnection> unsortedConnections = new ArrayList<>();
        
        for (final File connectionFile : Constants.CONNECTION_SETTINGS_DIR.listFiles()) {
            final ESConnection connection = jsonService.readValueFromFile(connectionFile, ESConnection.class);
            unsortedConnections.add(connection);
        }
        
        Collections.sort(unsortedConnections);
        
        connections.clear();
        connections.addAll(unsortedConnections);
    }
    
}
