package com.github.ormfux.esi.controller;

import java.util.List;

import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.model.index.ESIndexSettings;
import com.github.ormfux.esi.model.index.mapping.ESIndexMappingProperty;
import com.github.ormfux.esi.service.ESRestClient;
import com.github.ormfux.esi.service.JsonService;
import com.github.ormfux.esi.service.ManageIndexService;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import lombok.Getter;
import lombok.Setter;

@Bean(singleton = false)
public class IndexDetailsController {
    
    private final ManageIndexService manageIndexService;
    
    private final JsonService jsonService;
    
    private final ESRestClient restClient;
    
    @Setter
    @Getter
    private ESIndex index;
    
    @BeanConstructor
    public IndexDetailsController(final ESRestClient restClient, final ManageIndexService manageIndexService, final JsonService jsonService) {
        this.manageIndexService = manageIndexService;
        this.restClient = restClient;
        this.jsonService = jsonService;
    }
    
    public ESSearchResult search(final String query) {
        final String esResponse = returnResponseContent(restClient.sendPostRequest(index.getConnection(), index.getName() + "/_search?pretty", query));
        
        return new ESSearchResult(esResponse, jsonService.createJsonFXTree(esResponse, 3));
    }
    
    public ESSearchResult searchDocument(final String documentId) {
        final String esResponse = returnResponseContent(restClient.sendGetRequest(index.getConnection(), index.getName() + "/_doc/" + documentId + "?pretty"));
        return new ESSearchResult(esResponse, jsonService.createJsonFXTree(esResponse, 300));
    }
    
    public String searchDocumentForUpdate(final String documentId) {
        final ESResponse response = restClient.sendGetRequest(index.getConnection(), index.getName() + "/_doc/" + documentId + "?pretty");
        
        if (response.isOk()) {
            return jsonService.findNodeAsString(response.getResponseBody(), "_source");
        }
        
        return null;
    }
    
    public String deleteDocument(final String documentId) {
        return returnResponseContent(restClient.sendDeleteRequest(index.getConnection(), index.getName() + "/_doc/" + documentId + "?pretty"));
    }
    
    public String saveDocument(final String documentId, final String document) {
        return returnResponseContent(restClient.sendPutRequest(index.getConnection(), index.getName() + "/_doc/" + documentId + "?pretty", document));
    }
    
    public List<ESIndexMappingProperty> lookupIndexMappings() {
        return manageIndexService.findIndexMappingProperties(index);
    }
    
    public ESIndexSettings lookupIndexSettings() {
        return manageIndexService.findIndexSettings(index);
    }
    
    public List<String> lookupIndexDocumentPropertyPaths() {
        return manageIndexService.findIndexMappingPropertyPaths(index);
    }
    
    public List<String> lookupIndexAliasNames() {
        return manageIndexService.findIndexAliasNames(index);
    }
    
    public String lookupElasticsearchVersion() {
        return manageIndexService.findElasticsearchVersion(index);
    }
    
    private String returnResponseContent(final ESResponse response) {
        if (response.isOk()) {
            return response.getResponseBody();
        } else {
            return response.getResponseMessage();
        }
    }
    
}
