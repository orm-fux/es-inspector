package com.github.ormfux.esi.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.model.alias.ESMultiIndexAlias;
import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.model.session.InspectorSession;
import com.github.ormfux.esi.model.session.SessionAliasDetailsTabData;
import com.github.ormfux.esi.model.session.SessionGMTabData;
import com.github.ormfux.esi.model.session.SessionIndexDetailsTabData;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.service.JsonService;
import com.github.ormfux.esi.service.ManageAliasService;
import com.github.ormfux.esi.service.ManageIndexService;
import com.github.ormfux.esi.util.Constants;
import com.github.ormfux.esi.util.EncryptionUtils;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

@Bean
public class InspectorSessionController {
    
    private static final File SESSION_FILE = new File(Constants.SETTINGS_DIR, "session.dat");
    
    private final ManageConnectionsController connectionsController;
    
    private final ManageIndexService indexService;
    
    private final ManageAliasService aliasService;
    
    private final JsonService jsonService;
    
    @BeanConstructor
    public InspectorSessionController(final ManageConnectionsController connectionsController,
                                      final ManageIndexService indexService,
                                      final ManageAliasService aliasService, 
                                      final JsonService jsonService) {
        this.jsonService = jsonService;
        this.connectionsController = connectionsController;
        this.indexService = indexService;
        this.aliasService = aliasService;
    }
    
    public InspectorSession loadSession() {
        if (SESSION_FILE.isFile()) {
            try {
                final String sessionJson = EncryptionUtils.decodeText(Files.readString(SESSION_FILE.toPath()));
                
                return jsonService.readValueFromString(sessionJson, InspectorSession.class);
                
            } catch (final IOException e) {
                throw new ApplicationException("Unable to read inspector session data", e);
            }
        } else {
            return new InspectorSession();
        }
    }
    
    public void saveSession(final InspectorSession session) {
        if (SESSION_FILE.exists()) {
            SESSION_FILE.delete();
        }
        
        final String sessionJson = EncryptionUtils.encodeText(jsonService.writeValueAsString(session));
        
        try {
            Files.writeString(SESSION_FILE.toPath(), sessionJson);
        } catch (IOException e) {
            throw new ApplicationException("Error saving inspector session", e);
        }
    }

    public ESMultiIndexAlias lookupAlias(final SessionAliasDetailsTabData aliasTabData) {
        final ESConnection connection = lookupConnection(aliasTabData.getConnectionId());
        
        if (connection != null) {
            return aliasService.findAllAliases(connection)
                               .stream()
                               .filter(alias -> alias.getName().equals(aliasTabData.getAliasName()))
                               .findFirst()
                               .orElse(null);
        }
        return null;
    }

    public ESIndex lookupIndex(final SessionIndexDetailsTabData indexTabData) {
        final ESConnection connection = lookupConnection(indexTabData.getConnectionId());
        
        if (connection != null) {
            return indexService.findAllIndices(connection)
                               .stream()
                               .filter(index -> index.getName().equals(indexTabData.getIndexName()))
                               .findFirst()
                               .orElse(null);
        }
        
        return null;
    }

    public ESConnection lookupConnection(final SessionGMTabData godModeTabData) {
        return lookupConnection(godModeTabData.getConnectionId());
    }
    
    private ESConnection lookupConnection(final String connectionId) {
        return connectionsController.getAllConnections()
                                    .stream()
                                    .filter(connection -> connection.getId().equals(connectionId))
                                    .findFirst()
                                    .orElse(null);
    }
    
}
