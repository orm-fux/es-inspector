package com.github.ormfux.esi.service;

import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.LogEntry;
import com.github.ormfux.esi.model.LogEntry.Level;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.simple.di.annotations.Bean;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

@Bean
public class LoggingService {
    
    @Getter
    private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
    
    public void addLogEntry(ESResponse esResponse) {
        final LogEntry logEntry = new LogEntry();
        
        if (esResponse.isOk()) {
            logEntry.setLevel(Level.INFO);
        } else {
            logEntry.setLevel(Level.WARN);
        }
        
        if (esResponse.getResponseMessage() == null) {
            logEntry.setMessage(esResponse.getResponseCode() + "");
        } else {
            logEntry.setMessage(esResponse.getResponseCode() + ": " + esResponse.getResponseMessage());
        }
        
        if (esResponse.getResponseBody() != null) {
            logEntry.setDetails(esResponse.getResponseBody().replaceAll("(\n\r)|\n|\r", " "));
            logEntry.setDetailsUnformatted(esResponse.getResponseBody());
        }
        
        addLogEntry(logEntry);
    }
    
    public void addLogEntry(final ESConnection connection, final String httpMethod, final String endpoint, final String requestBody) {
        final LogEntry logEntry = new LogEntry();
        
        logEntry.setLevel(Level.INFO);
        
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(httpMethod).append(' ').append(connection.getUrl()).append(endpoint);
        logEntry.setMessage(messageBuilder.toString());
        
        if (requestBody != null) {
            logEntry.setDetails(requestBody.replaceAll("(\n\r)|\n|\r", " "));
            logEntry.setDetailsUnformatted(requestBody);
        }
        
        addLogEntry(logEntry);
    }
    
    public void addLogEntry(final Throwable exception) {
        exception.printStackTrace();
        final LogEntry logEntry = new LogEntry();
        logEntry.setLevel(Level.ERROR);
        logEntry.setMessage(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        
        if (exception.getCause() != null && exception.getCause() != exception) {
            logEntry.setDetails(exception.getCause().getMessage());
        }
        
        addLogEntry(logEntry);
    }
    
    public synchronized void addLogEntry(final LogEntry logEntry) {
        Platform.runLater(() -> {
            logEntries.add(logEntry);
            capSize();
        });
    }

    private void capSize() {
        while (logEntries.size() > 25) {
            logEntries.remove(0);
        }
    }
    
}
