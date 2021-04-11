package com.github.ormfux.esi.service;

import java.util.HashMap;
import java.util.Map;

import com.github.ormfux.simple.di.annotations.Bean;

@Bean
public class ESConnectionUsageStatusService {
    
    private final Map<String, Integer> connectionUsageCounts = new HashMap<>();
    
    public boolean isConnected(final String connectionId) {
        return connectionUsageCounts.getOrDefault(connectionId, 0) > 0;
    }
    
    public void connectionOpened(final String connectionId) {
        //System.out.println("connection opened: " + connectionId);
        connectionUsageCounts.compute(connectionId, (id, count) -> count == null ? 1 : count + 1);
    }
    
    public void connectionClosed(final String connectionId) {
        //System.out.println("connection closed: " + connectionId);
        connectionUsageCounts.compute(connectionId, (id, count) -> count == null ? 0 : count - 1);
    }
    
}
