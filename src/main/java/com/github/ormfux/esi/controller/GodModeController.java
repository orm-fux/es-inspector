package com.github.ormfux.esi.controller;

import java.util.function.Consumer;

import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.service.ESRestClient;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import lombok.Getter;
import lombok.Setter;

@Bean(singleton = false)
public class GodModeController {

    @Setter
    @Getter
    private ESConnection connection; 
    
    private final ESRestClient restClient;
    
    @Setter
    private Consumer<ESConnection> detailsViewOpener;
    
    @BeanConstructor
    public GodModeController(final ESRestClient restClient) {
        this.restClient = restClient;
    }
    
    public ESResponse executeRequest(final String method, final String endpoint, final String requestBody) {
        return restClient.sendRequest(connection, method, endpoint, (requestBody != null && requestBody.isBlank()) ? null : requestBody);
    }
    
}
