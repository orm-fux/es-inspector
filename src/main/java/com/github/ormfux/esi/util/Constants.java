package com.github.ormfux.esi.util;

import java.io.File;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    private static final File SETTINGS_DIR = new File(System.getProperty("user.home") + "/.esinspector"); 
    
    public static final File CONNECTION_SETTINGS_DIR = new File(SETTINGS_DIR, "connections"); 
    
    public static final File QUERY_TEMPLATES_SETTINGS_DIR = new File(SETTINGS_DIR, "querytemplates"); 
    
}
