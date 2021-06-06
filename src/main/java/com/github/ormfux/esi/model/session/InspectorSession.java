package com.github.ormfux.esi.model.session;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InspectorSession {
    
    private List<SessionTabData> tabData = new ArrayList<>();
    
    public void addTabData(final SessionTabData tab) {
        tabData.add(tab);
    }
}
