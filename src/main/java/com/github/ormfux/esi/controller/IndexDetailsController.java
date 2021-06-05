package com.github.ormfux.esi.controller;

import java.util.List;

import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.ESSearchResult;
import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.model.index.ESIndexSettings;
import com.github.ormfux.esi.model.index.mapping.ESIndexMappingProperty;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.service.ESRestClient;
import com.github.ormfux.esi.service.JsonService;
import com.github.ormfux.esi.service.ManageIndexService;
import com.github.ormfux.esi.service.QueryResultTransformService;
import com.github.ormfux.esi.service.QueryTemplateService;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import lombok.Getter;
import lombok.Setter;

@Bean(singleton = false)
public class IndexDetailsController extends AbstractQueryTemplateController {
    
    private final ManageIndexService manageIndexService;
    
    private QueryResultTransformService resultTransformService;
    
    private final JsonService jsonService;
    
    private final ESRestClient restClient;
    
    @Setter
    @Getter
    private ESIndex index;
    
    @BeanConstructor
    public IndexDetailsController(final ESRestClient restClient, 
                                  final ManageIndexService manageIndexService,
                                  final QueryResultTransformService resultTransformService,
                                  final JsonService jsonService,
                                  final QueryTemplateService queryTemplateService) {
        super(queryTemplateService);
        this.manageIndexService = manageIndexService;
        this.restClient = restClient;
        this.resultTransformService = resultTransformService;
        this.jsonService = jsonService;
    }
    
    public ESSearchResult search(final String query) {
        final String esResponse = returnResponseContent(restClient.sendPostRequest(index.getConnection(), index.getName() + "/_search?pretty", query));
        
        return new ESSearchResult(esResponse, resultTransformService.createJsonFXTree(esResponse, 3), resultTransformService.createTable(esResponse));
    }
    
    public ESSearchResult searchDocument(final String documentId) {
        final String esResponse = returnResponseContent(doSearchDocument(documentId));
        return new ESSearchResult(esResponse, resultTransformService.createJsonFXTree(esResponse, 300), resultTransformService.createTable(esResponse));
    }

    public String searchDocumentForUpdate(final String documentId) {
        final ESResponse response = doSearchDocument(documentId);
        
        if (response.isOk()) {
            return jsonService.findNodeAsString(response.getResponseBody(), "_source");
        }
        
        return null;
    }
    
    public String deleteDocument(final String documentId) {
        return returnResponseContent(restClient.sendDeleteRequest(index.getConnection(), index.getName() + "/_doc/" + documentId + "?pretty"));
    }
    
    public String saveDocument(final String documentId, final String document, final boolean update) {
        if (!update && doSearchDocument(documentId).isOk()) {
            return "A document with id '" + documentId + "' already exists";
        } else {
            return returnResponseContent(restClient.sendPutRequest(index.getConnection(), index.getName() + "/_doc/" + documentId + "?pretty", document));
        }
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
    
    @Override
    protected ESConnection getESConnection() {
        return index.getConnection();
    }
    
    private ESResponse doSearchDocument(final String documentId) {
        return restClient.sendGetRequest(index.getConnection(), index.getName() + "/_doc/" + documentId + "?pretty");
    }
    
    private String returnResponseContent(final ESResponse response) {
        if (response.isOk()) {
            return response.getResponseBody();
        } else {
            return response.getResponseMessage();
        }
    }
    
}
