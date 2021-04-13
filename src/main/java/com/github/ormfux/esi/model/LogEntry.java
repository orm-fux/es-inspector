package com.github.ormfux.esi.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class LogEntry {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    
    private Level level;
    
    private String message;
    
    private String details;
    
    private String detailsUnformatted;
    
    public enum Level {
        INFO,
        
        WARN,
        
        ERROR
    }
    
}
