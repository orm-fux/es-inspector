package com.github.ormfux.esi.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.alias.ESMultiIndexAlias;
import com.github.ormfux.esi.model.alias.ESSingleIndexAlias;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

@Bean
public class ManageAliasService {
    
    private final JsonService jsonService;
    
    private final ESRestClient restClient;
    
    @BeanConstructor
    public ManageAliasService(final ESRestClient restClient, final JsonService jsonService) {
        this.restClient = restClient;
        this.jsonService = jsonService;
    }
    
    public List<ESMultiIndexAlias> findAllAliases(final ESConnection connection) {
        final ESResponse response = restClient.sendGetRequest(connection, "_cat/aliases?format=json");
        
        if (response.isOk()) {
            final ESSingleIndexAlias[] ungroupedAliases = jsonService.readValueFromString(response.getResponseBody(), ESSingleIndexAlias[].class);
            
            return Arrays.stream(ungroupedAliases)
                         .collect(groupingBy(ESSingleIndexAlias::getName))
                         .entrySet()
                         .stream()
                         .map(groupedByIndices -> new ESMultiIndexAlias(connection, 
                                                                        groupedByIndices.getKey(),
                                                                        
                                                                        groupedByIndices.getValue()
                                                                                        .stream()
                                                                                        .map(ESSingleIndexAlias::getIndex)
                                                                                        .sorted()
                                                                                        .collect(toList()),
                                                                                        
                                                                        groupedByIndices.getValue()
                                                                                        .stream()
                                                                                        .filter(index -> Boolean.getBoolean(index.getWriteIndex()))
                                                                                        .findFirst()
                                                                                        .map(ESSingleIndexAlias::getIndex)
                                                                                        .orElse(null)))
                         .sorted()
                         .collect(toList());
            
        } else {
            return Collections.emptyList();
        }
    }
    
    public void deleteAlias(final ESConnection connection, final String aliasName, final String indexName) {
        restClient.sendDeleteRequest(connection, indexName + "/_alias/" + aliasName);
    }
    
    public void createAlias(final ESConnection connection, final ESSingleIndexAlias aliasProperties) {
        final String requestBody = jsonService.writeValueAsString(aliasProperties);
        restClient.sendPutRequest(connection, aliasProperties.getIndex() + "/_alias/" + aliasProperties.getName(), requestBody);
    }
    
    public String findElasticsearchVersion(final ESMultiIndexAlias alias) {
        return restClient.ping(alias.getConnection()).orElse(null);
    }
    
}
