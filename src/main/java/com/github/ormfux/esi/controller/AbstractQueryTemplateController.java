package com.github.ormfux.esi.controller;

import java.util.List;

import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.model.template.QueryTemplate;
import com.github.ormfux.esi.service.QueryTemplateService;

public abstract class AbstractQueryTemplateController {
    
    private final QueryTemplateService queryTemplateService;
    
    public AbstractQueryTemplateController(final QueryTemplateService queryTemplateService) {
        this.queryTemplateService = queryTemplateService;
    }
    
    public void createQueryTemplate(final String name, final boolean global, final String query) {
        final QueryTemplate template = new QueryTemplate();
        template.setName(name);
        template.setQuery(query);
        
        queryTemplateService.saveTemplate(global ? null : getESConnection().getId(), template);
    }
    
    public void deleteQueryTemplate(final boolean global, final QueryTemplate template) {
        queryTemplateService.deleteTemplate(global ? null : getESConnection().getId(), template);
    }
    
    public void updateQueryTemplate(final boolean global, final QueryTemplate template) {
        queryTemplateService.saveTemplate(global ? null : getESConnection().getId(), template);
    }
    
    public List<QueryTemplate> lookupGlobalQueryTemplates() {
        return queryTemplateService.loadGlobalTemplates();
    }
    
    public List<QueryTemplate> lookupConnectionQueryTemplates() {
        return queryTemplateService.loadConnectionTemplates(getESConnection().getId());
    }
    
    protected abstract ESConnection getESConnection();
    
}
