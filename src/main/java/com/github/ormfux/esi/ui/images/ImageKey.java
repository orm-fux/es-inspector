package com.github.ormfux.esi.ui.images;

import lombok.AccessLevel;
import lombok.Getter;

public enum ImageKey {
    
    CREATE("create.png"),
    
    DELETE("delete.png"),
    
    EDIT("edit.png"),

    CONNECT("connect.png"),
    
    GOD_MODE("god_mode.png"),
    
    LIGHTNING("lightning.png"),
    
    INDEX("index.png"),
    
    ALIAS("alias.png"),
    
    APPLICATION("esg-icon.png");

    @Getter(AccessLevel.PROTECTED)
    private final String fileName;
    
    private ImageKey(final String fileName) {
        this.fileName = fileName;
    }
    
}