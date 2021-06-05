package com.github.ormfux.esi.service;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.github.ormfux.esi.model.template.QueryTemplate;
import com.github.ormfux.esi.model.template.QueryTemplateCollection;
import com.github.ormfux.esi.util.Constants;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

@Bean
public class QueryTemplateService {

    private static final File GLOBAL_TEMPLATES_FILE = new File(Constants.QUERY_TEMPLATES_SETTINGS_DIR, "global-templates.json");
    
    private final JsonService jsonService;
    
    @BeanConstructor
    public QueryTemplateService(final JsonService jsonService) {
        this.jsonService = jsonService;
        
        if (!Constants.QUERY_TEMPLATES_SETTINGS_DIR.exists()) {
            Constants.QUERY_TEMPLATES_SETTINGS_DIR.mkdirs();
        }
    }
    
    public List<QueryTemplate> loadGlobalTemplates() {
        if (GLOBAL_TEMPLATES_FILE.exists()) {
            return jsonService.readValueFromFile(GLOBAL_TEMPLATES_FILE, QueryTemplateCollection.class).getTemplates();
            
        } else {
            final QueryTemplateCollection globalTemplates = new QueryTemplateCollection();
            globalTemplates.addTemplate(new QueryTemplate(createId(), "Match All", "{\n  \"from\": 0,\n  \"size\": 10,\n  \"query\": {\n    \"match_all\": {}\n  },\n  \"sort\": [],\n  \"aggs\": {}\n}"));
            globalTemplates.addTemplate(new QueryTemplate(createId(), "Match", "{\n  \"from\": 0,\n  \"size\": 10,\n  \"query\": {\n    \"match\": {\n      \"$fieldName\": \"$value\"\n    }\n  }\n}"));
            globalTemplates.addTemplate(new QueryTemplate(createId(), "Bool", "{\n  \"from\": 0,\n  \"size\": 10,\n  \"query\": {\n    \"bool\": {\n      \"must\": [],\n      \"must_not\": [],\n      \"should\": [],\n      \"minimum_should_match\": 1\n    }\n  }\n}"));
            
            jsonService.writeToFile(GLOBAL_TEMPLATES_FILE, globalTemplates);
            
            return globalTemplates.getTemplates();
        }
    }
    
    public List<QueryTemplate> loadConnectionTemplates(final String connectionId) {
        return loadTemplateCollection(getTemplateFile(connectionId), connectionId).getTemplates();
    }
    
    public void deleteTemplateCollection(final String connectionId) {
        final File templatesFile = getTemplateFile(connectionId);
        
        if (templatesFile.exists()) {
            templatesFile.delete();
        }
    }
    
    public void deleteTemplate(final String connectionId, final QueryTemplate template) {
        final File templatesFile = getTemplateFile(connectionId);
        
        final QueryTemplateCollection templateCollection = loadTemplateCollection(templatesFile, connectionId);
        templateCollection.removeTemplate(template.getId());
        
        jsonService.writeToFile(templatesFile, templateCollection);
    }
    
    public void saveTemplate(final String connectionId, final QueryTemplate template) {
        if (template.getId() == null) {
            saveNewTemplate(connectionId, template);
        } else {
            updateExistingTemplate(connectionId, template);
        }
    }
    
    private void saveNewTemplate(final String connectionId, final QueryTemplate template) {
        final File templatesFile = getTemplateFile(connectionId);
        
        final QueryTemplateCollection templatesCollection = loadTemplateCollection(templatesFile, connectionId);
        template.setId(createId());
        templatesCollection.addTemplate(template);
        
        jsonService.writeToFile(templatesFile, templatesCollection);
    }

    private void updateExistingTemplate(final String connectionId, final QueryTemplate template) {
        final File templatesFile = getTemplateFile(connectionId);
        
        final QueryTemplateCollection templateCollection = loadTemplateCollection(templatesFile, connectionId);
        templateCollection.removeTemplate(template.getId());
        templateCollection.addTemplate(template);
        
        jsonService.writeToFile(templatesFile, templateCollection);
    }
    
    private QueryTemplateCollection loadTemplateCollection(final File templatesFile, final String connectionId) {
        if (templatesFile.exists()) {
            return jsonService.readValueFromFile(templatesFile, QueryTemplateCollection.class);
        } else {
            final QueryTemplateCollection templateCollection = new QueryTemplateCollection();
            templateCollection.setConnectionId(connectionId);
            
            jsonService.writeToFile(templatesFile, templateCollection);
            
            return templateCollection;
        }
    }

    private File getTemplateFile(final String connectionId) {
        final File templatesFile;
        
        if (connectionId == null) {
            templatesFile = GLOBAL_TEMPLATES_FILE;
        } else {
            templatesFile = new File(Constants.QUERY_TEMPLATES_SETTINGS_DIR, connectionId + ".json");
        }
        return templatesFile;
    }

    private String createId() {
        return UUID.randomUUID().toString();
    }
    
}
