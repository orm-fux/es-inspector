package com.github.ormfux.esi.model.settings.connection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiKeyAuthentication implements Authentication {

    private String apiKey;

    @Override
    @JsonIgnore
    public String getAuthenticationHeaderValue() {
        return apiKey;
    }

    @Override
    @JsonIgnore
    public String getType() {
        return "ApiKey";
    }

}
