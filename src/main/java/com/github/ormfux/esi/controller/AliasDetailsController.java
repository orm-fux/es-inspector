package com.github.ormfux.esi.controller;

import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.model.alias.ESMultiIndexAlias;
import com.github.ormfux.esi.service.ESRestClient;
import com.github.ormfux.esi.service.JsonService;
import com.github.ormfux.esi.service.ManageAliasService;
import com.github.ormfux.esi.service.QueryResultTransformService;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import lombok.Getter;
import lombok.Setter;

@Bean(singleton = false)
public class AliasDetailsController {
    
    private final ManageAliasService manageAliasService;
    
    private final QueryResultTransformService resultTransformService;
    
    private final JsonService jsonService;
    
    private final ESRestClient restClient;
    
    @Setter
    @Getter
    private ESMultiIndexAlias alias;
    
    @BeanConstructor
    public AliasDetailsController(final ESRestClient restClient, 
                                  final ManageAliasService manageAliasService,
                                  final QueryResultTransformService resultTransformService,
                                  final JsonService jsonService) {
        this.manageAliasService = manageAliasService;
        this.restClient = restClient;
        this.resultTransformService = resultTransformService;
        this.jsonService = jsonService;
    }
    
    public ESSearchResult search(final String query) {
        final String esResponse = returnResponseContent(doSearch(query));
        return new ESSearchResult(esResponse, resultTransformService.createJsonFXTree(esResponse, 3), resultTransformService.createTable(esResponse));
    }

    private ESResponse doSearch(final String query) {
        return restClient.sendPostRequest(alias.getConnection(), alias.getName() + "/_search?pretty", query);
    }
    
    public ESSearchResult searchDocument(final String documentId) {
        final String esResponse = returnResponseContent(doSearchDocument(documentId));
        return new ESSearchResult(esResponse, resultTransformService.createJsonFXTree(esResponse, 300), resultTransformService.createTable(esResponse));
    }

    private ESResponse doSearchDocument(final String documentId) {
        return doSearch("{\"query\": { \"match\": { \"_id\": \"" + documentId + "\"}}}");
    }
    
    public String searchDocumentForUpdate(final String documentId) {
        final ESResponse response = doSearchDocument(documentId);
        
        if (response.isOk()) {
            return jsonService.findNodeAsString(response.getResponseBody(), "_source");
        }
        
        return null;
    }
    
    public String deleteDocument(final String documentId) {
        return returnResponseContent(restClient.sendDeleteRequest(alias.getConnection(), alias.getName() + "/_doc/" + documentId + "?pretty"));
    }
    
    public String saveDocument(final String documentId, final String document, final boolean update) {
        if (!update && doSearchDocument(documentId).isOk()) {
            throw new ApplicationException("A document with id '" + documentId + "' already exists", null);
        } else {
            return returnResponseContent(restClient.sendPutRequest(alias.getConnection(), alias.getName() + "/_doc/" + documentId + "?pretty", document));
        }
    }
    
    public String lookupElasticsearchVersion() {
        return manageAliasService.findElasticsearchVersion(alias);
    }
    
    private String returnResponseContent(final ESResponse response) {
        if (response.isOk()) {
            return response.getResponseBody();
        } else {
            return response.getResponseMessage();
        }
    }
    
}
