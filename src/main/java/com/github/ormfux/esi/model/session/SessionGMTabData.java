package com.github.ormfux.esi.model.session;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SessionGMTabData extends SessionTabData {
    
    private String httpMethod;
    
    private String endpoint;
    
    private String requestBody;
    
}
