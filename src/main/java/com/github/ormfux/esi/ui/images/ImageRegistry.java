package com.github.ormfux.esi.ui.images;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageRegistry {
    
    private static final Map<ImageKey, Image> IMAGES = new ConcurrentHashMap<>();
    
    public static Image getImage(final ImageKey key) {
        if (!IMAGES.containsKey(key)) {
            IMAGES.put(key, new Image(ImageRegistry.class.getResourceAsStream(key.getFileName())));
        }
        
        return IMAGES.get(key);
    }
    
    public static ImageView getImageView(final ImageKey key, final double width, final double height) {
        final ImageView imageView = new ImageView(getImage(key));
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        
        return imageView;
    }
    
}