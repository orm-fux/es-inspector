package com.github.ormfux.esi.ui.component;

import com.github.ormfux.esi.model.session.SessionTabData;
import com.github.ormfux.esi.ui.images.ImageButton;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.images.ImageRegistry;

import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class RestorableTab extends Tab {
    
    private boolean restore;
    
    private final ImageButton pinButton = new ImageButton(ImageKey.PIN, 18);
    private final ImageButton unpinButton = new ImageButton(ImageKey.UNPIN, 18);
    
    public RestorableTab() {
        setClosable(true);
        
        final ImageView tabIcon = new ImageView(ImageRegistry.getImage(getTabIconKey()));
        tabIcon.setFitHeight(23);
        tabIcon.setFitWidth(23);
        
        unpinButton.setOnAction(e -> setRestore(true));
        pinButton.setOnAction(e -> setRestore(false));
        
        final HBox tabGraphics = new HBox(3);
        tabGraphics.setAlignment(Pos.CENTER_LEFT);
        tabGraphics.getChildren().addAll(pinButton, unpinButton, tabIcon);
        
        setRestore(false);
        setGraphic(tabGraphics);
    }
    
    public void setRestore(final boolean restore) {
        this.restore = restore;
        unpinButton.setVisible(!restore);
        unpinButton.setManaged(!restore);
        pinButton.setVisible(restore);
        pinButton.setManaged(restore);
    }
    
    protected abstract ImageKey getTabIconKey();
    
    public abstract SessionTabData getRestorableData();
    
    public abstract void fillWithRestoreData(final SessionTabData tabData);
}
