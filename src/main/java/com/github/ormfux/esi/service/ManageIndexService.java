package com.github.ormfux.esi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.model.index.ESIndexSettings;
import com.github.ormfux.esi.model.index.mapping.ESIndexMapping;
import com.github.ormfux.esi.model.index.mapping.ESIndexMappingProperty;
import com.github.ormfux.esi.model.index.mapping.ESIndexMappingPropertyField;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

@Bean
public class ManageIndexService {

    private final JsonService jsonService;
    
    private final ESRestClient restClient;

    @BeanConstructor
    public ManageIndexService(final ESRestClient restClient, final JsonService jsonService) {
        this.restClient = restClient;
        this.jsonService = jsonService;
    }
    
    public List<ESIndex> findAllIndices(final ESConnection connection) {
        final ESResponse response = restClient.sendGetRequest(connection, "_cat/indices?v&format=json");
        
        if (response.isOk()) {
            final ESIndex[] indices = jsonService.readValueFromString(response.getResponseBody(), ESIndex[].class);
            
            final List<ESIndex> sortedIndices = Stream.of(indices).sorted().collect(Collectors.toList());
            sortedIndices.forEach(index -> index.setConnection(connection));
            
            return sortedIndices;
        } else {
            return Collections.emptyList();
        }
    }
    
    public List<String> findIndexAliasNames(final ESIndex index) {
        final ESResponse response = restClient.sendGetRequest(index.getConnection(), index.getName() + "/_alias/_all");
        
        if (response.isOk()) {
            final JsonNode aliasesNode = jsonService.readTreeFromPath(response.getResponseBody(), "/" + index.getName() + "/aliases");
            
            if (!aliasesNode.isMissingNode()) {
                final ObjectNode responseTree = (ObjectNode) aliasesNode;
                final Iterator<String> aliasNamesIterator = responseTree.fieldNames();
                
                final List<String> aliasNames = new ArrayList<>();
                
                while (aliasNamesIterator.hasNext()) {
                    aliasNames.add(aliasNamesIterator.next());
                }
                
                Collections.sort(aliasNames);
                
                return aliasNames;
            } else {
                return Collections.emptyList();
            }
            
        } else {
            return Collections.emptyList();
        }
        
    }
    
    public List<String> findIndexMappingPropertyPaths(final ESIndex index) {
        final ESIndexMapping mapping = findIndexMapping(index);
        
        if (mapping != null) {
            final List<String> paths = mapToPropertyPaths(mapping, null);
            Collections.sort(paths);
            return paths;
            
        } else {
            return Collections.emptyList();
        }
    }
    
    private List<String> mapToPropertyPaths(final ESIndexMapping mapping, final String prefix) {
        final List<String> paths = new ArrayList<>();
        
        if (mapping.getProperties() != null) {
            for (final Entry<String, ESIndexMapping> nestedProperty : mapping.getProperties().getProperties().entrySet()) {
                final String nestingPrefix;
                
                if (prefix != null) {
                    nestingPrefix = prefix + "." + nestedProperty.getKey();
                } else {
                    nestingPrefix = nestedProperty.getKey();
                }
                
                paths.addAll(mapToPropertyPaths(nestedProperty.getValue(), nestingPrefix));
                
            }
            
        } else if (prefix != null) {
            paths.add(prefix);
            
            if (mapping.getFields() != null) {
                mapping.getFields().getFields().keySet().stream().map(field -> prefix + "." + field).forEach(paths::add);
            }
        }
        
        return paths;
    }
    
    public List<ESIndexMappingProperty> findIndexMappingProperties(final ESIndex index) {
        final ESIndexMapping mapping = findIndexMapping(index);
        
        if (mapping != null) {
            final List<ESIndexMappingProperty> paths = mapToProperties(mapping, null);
            Collections.sort(paths);
            return paths;
            
        } else {
            return Collections.emptyList();
        }
    }
    
    private List<ESIndexMappingProperty> mapToProperties(final ESIndexMapping mapping, final String prefix) {
        final List<ESIndexMappingProperty> paths = new ArrayList<>();
        
        if (mapping.getProperties() != null) {
            for (final Entry<String, ESIndexMapping> nestedProperty : mapping.getProperties().getProperties().entrySet()) {
                final String nestingPrefix;
                
                if (prefix != null) {
                    nestingPrefix = prefix + "." + nestedProperty.getKey();
                } else {
                    nestingPrefix = nestedProperty.getKey();
                }
                
                paths.addAll(mapToProperties(nestedProperty.getValue(), nestingPrefix));
                
            }
            
        } else if (prefix != null) {
            final ESIndexMappingProperty property = new ESIndexMappingProperty();
            property.setPath(prefix);
            property.setType(mapping.getType());
            
            if (mapping.getFields() != null) {
                property.setFields(mapping.getFields()
                                          .getFields()
                                          .entrySet()
                                          .stream()
                                          .map(field -> new ESIndexMappingPropertyField(field.getKey(), field.getValue().getType()))
                                          .sorted()
                                          .collect(Collectors.toList()));
            }
            
            paths.add(property);
        }
        
        return paths;
    }
    
    public ESIndexMapping findIndexMapping(final ESIndex index) {
        final ESResponse mappingResponse = restClient.sendGetRequest(index.getConnection(), index.getName() + "/_mapping?include_type_name=false");
        
        if (mappingResponse.isOk()) {
            final JsonNode mappingRoot = jsonService.readTreeFromPath(mappingResponse.getResponseBody(), "/" + index.getName() + "/mappings");
            
            if (!mappingRoot.isMissingNode()) {
                return jsonService.readValueFromString(mappingRoot.toString(), ESIndexMapping.class);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public ESIndexSettings findIndexSettings(final ESIndex index) {
        final ESResponse response = restClient.sendGetRequest(index.getConnection(), index.getName() + "/_settings?flat_settings=true&pretty");
        
        if (response.isOk()) {
            final JsonNode settingsRoot = jsonService.readTreeFromPath(response.getResponseBody(), "/" + index.getName() + "/settings");
            
            if (!settingsRoot.isMissingNode()) {
                final ESIndexSettings settings = jsonService.readValueFromString(settingsRoot.toString(), ESIndexSettings.class);
                settings.setRawSettings(response.getResponseBody());
                
                return settings;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public String findElasticsearchVersion(final ESIndex index) {
        return restClient.ping(index.getConnection()).orElse(null);
    }
    
    public void createIndex(final ESConnection connection, final String name, final String indexProperties) {
        restClient.sendPutRequest(connection, name, indexProperties);
    }

    public void deleteIndex(final ESConnection connection, final String name) {
        restClient.sendDeleteRequest(connection, name);
    }

}
