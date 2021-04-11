package com.github.ormfux.esi.model.settings.connection;

import java.util.Base64;

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
public class BasicAuthentication implements Authentication {

    private String username;

    @JsonSerialize(using = JsonEncryptSerializer.class)
    @JsonDeserialize(using = JsonDecryptDeserializer.class)
    private String password;

    @Override
    @JsonIgnore
    public String getAuthenticationHeaderValue() {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Override
    @JsonIgnore
    public String getType() {
        return "Basic";
    }

}
