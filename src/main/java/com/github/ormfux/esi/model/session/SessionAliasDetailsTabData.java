package com.github.ormfux.esi.model.session;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SessionAliasDetailsTabData extends SessionTabData {
    
    private String aliasName;
    
    private DetailsTab selectedTab;
    
    private String plainQuery;
    
}
