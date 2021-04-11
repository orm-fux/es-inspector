package com.github.ormfux.esi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.settings.connection.Authentication;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

//FIXME Switch to HttpClient
@Bean
public class ESRestClient {
    
    private final JsonService jsonService;
    
    private final LoggingService loggingService;
    
    @BeanConstructor
    public ESRestClient(final LoggingService loggingService, final JsonService jsonService) {
        this.loggingService = loggingService;
        this.jsonService = jsonService;
    }
    
    public Optional<String> ping(final ESConnection connection) {
        final ESResponse response = sendGetRequest(connection, null);
        
        if (response.isOk()) {
            return Optional.of(jsonService.readStringFromPath(response.getResponseBody(), "/version/number"));
        } else {
            return Optional.empty();
        }
        
    }

    public ESResponse sendGetRequest(final ESConnection connection, final String endpoint) {
        return sendRequest(connection, "GET", endpoint, null);
    }
    
    public ESResponse sendPostRequest(final ESConnection connection, final String endpoint, final String requestBody) {
        return sendRequest(connection, "POST", endpoint, requestBody);
    }
    
    public ESResponse sendPutRequest(final ESConnection connection, final String endpoint, final String requestBody) {
        return sendRequest(connection, "PUT", endpoint, requestBody);
    }
    
    public ESResponse sendDeleteRequest(final ESConnection connection, final String endpoint) {
        return sendRequest(connection, "DELETE", endpoint, null);
    }
    
    public ESResponse sendRequest(final ESConnection connection, final String httpMethod, final String endpoint, final String requestBody) {
        try {
            final String cleanedEndpoint = endpoint != null && !endpoint.isBlank() ? ("/" + endpoint) : "";
            
            loggingService.addLogEntry(connection, httpMethod, cleanedEndpoint, requestBody);
            
            final URL url = new URL(connection.getUrl() + cleanedEndpoint);
            final HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(httpMethod);
            http.setDoOutput(true);
            http.setRequestProperty("Accept", "application/json");
            http.setRequestProperty("Content-Type", "application/json");
            
            final Authentication authentication = connection.getAuthentication();
            
            if (authentication != null) {
                http.setRequestProperty("Authorization", authentication.getType() + " " + authentication.getAuthenticationHeaderValue());
            }
            
            ESResponse response = null;
    
            try {
                if (requestBody != null) {
                    final OutputStream stream = http.getOutputStream();
                    stream.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }
                
                if (http.getResponseCode() >= 200 && http.getResponseCode() < 300) {
                    InputStream inputStream = http.getInputStream();
                    
                    final String responseBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                                .lines()
                                .collect(Collectors.joining("\n"));
                    response = new ESResponse(http.getResponseCode(), http.getResponseMessage(), responseBody);
                } else {
                    response = new ESResponse(http.getResponseCode(), http.getResponseMessage(), null);
                }
                
                return response;
            } finally {
                if (response != null) {
                    loggingService.addLogEntry(response);
                }
                
                http.disconnect();
            }
        } catch (final IOException e) {
            throw new ApplicationException("Error executing request", e);
        }
    }
    
}
