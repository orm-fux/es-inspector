package com.github.ormfux.esi.model.settings.connection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.ormfux.esi.util.EncryptionUtils.JsonDecryptDeserializer;
import com.github.ormfux.esi.util.EncryptionUtils.JsonEncryptSerializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiKeyAuthentication implements Authentication {

    @JsonSerialize(using = JsonEncryptSerializer.class)
    @JsonDeserialize(using = JsonDecryptDeserializer.class)
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
