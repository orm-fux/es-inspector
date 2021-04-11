package com.github.ormfux.esi.model;

import lombok.Data;

@Data
public class ESResponse {
    
    private final int responseCode;
    
    private final String responseMessage;
    
    private final String responseBody;
    
    public boolean isOk() {
        return responseCode >= 200 && responseCode < 300;
    }
    
}
