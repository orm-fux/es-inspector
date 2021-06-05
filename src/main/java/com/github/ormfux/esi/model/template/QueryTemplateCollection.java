package com.github.ormfux.esi.model.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Data;

@Data
public class QueryTemplateCollection {

    private String connectionId;
    
    private List<QueryTemplate> templates = new ArrayList<>();
    
    public void addTemplate(final QueryTemplate template) {
        templates.add(template);
        Collections.sort(templates);
    }
    
    public void removeTemplate(final QueryTemplate template) {
        templates.remove(template);
    }
    
    public void removeTemplate(final String templateId) {
        templates.removeIf(template -> template.getId().equals(templateId));
    }
    
}
