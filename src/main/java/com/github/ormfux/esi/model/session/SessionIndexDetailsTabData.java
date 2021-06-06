package com.github.ormfux.esi.model.session;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SessionIndexDetailsTabData extends SessionTabData {
    
    private String indexName;
    
    private DetailsTab selectedTab;
    
    private QueryType selectedQueryType;
    
    private String plainQuery;
    
    private List<GuidedBooleanCondition> guidedBooleanQuery = new ArrayList<>();
    
}
