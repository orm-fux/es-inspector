package com.github.ormfux.esi.model.session;

import lombok.Data;

@Data
public class GuidedBooleanCondition {
    
    private String propertyName;
    
    private String condition;
    
    private String required;
    
    private String value;
    
    private String valueTo;
}
