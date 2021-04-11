package com.github.ormfux.esi.ui.images;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ImageButton extends Button {
    
    private static final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 5, 5, 5, 5;";
    
    private static final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 6 4 4 6;";
    
    private static final int ICON_SIZE = 23;
    
    public ImageButton(ImageKey image) {
        this(image, ICON_SIZE);
    }
    
    public ImageButton(final ImageKey image, final int iconSize) {
        final ImageView icon = new ImageView(ImageRegistry.getImage(image));
        
        icon.setFitHeight(iconSize);
        icon.setFitWidth(iconSize);
        setMaxWidth(iconSize);
        setMinWidth(iconSize);
        setMaxHeight(iconSize);
        setMinHeight(iconSize);
        
        setGraphic(icon);
        setStyle(STYLE_NORMAL);
        setCursor(Cursor.HAND);
        
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setStyle(STYLE_PRESSED);
            }            
        });
        
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
               setStyle(STYLE_NORMAL);
            }
        });
        
    }
    
}