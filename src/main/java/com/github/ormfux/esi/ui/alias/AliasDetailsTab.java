package com.github.ormfux.esi.ui.alias;

import java.util.function.Supplier;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.ui.ESConnectedView;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.images.ImageRegistry;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.ImageView;

public class AliasDetailsTab extends Tab implements ESConnectedView {
    
    private final Supplier<ESConnection> connectionSupplier;
    
    public AliasDetailsTab(final AliasDetailsController aliasController) {
        setClosable(true);
        
        final ImageView tabIcon = new ImageView(ImageRegistry.getImage(ImageKey.ALIAS));
        tabIcon.setFitHeight(23);
        tabIcon.setFitWidth(23);
        setGraphic(tabIcon);
        
        setText(aliasController.getAlias().getConnection().getName() + ": " + aliasController.getAlias().getName());
        
        final TabPane details = new TabPane();
        details.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        details.setSide(Side.LEFT);
        
        final AliasDetailsView detailsView = new AliasDetailsView(aliasController);
        final Tab detailsTab = new Tab("Details");
        detailsTab.setContent(detailsView);
        details.getTabs().add(detailsTab);
        
        final AliasDocumentView documentView = new AliasDocumentView(aliasController);
        final Tab documentTab = new Tab("Document");
        documentTab.setContent(documentView);
        details.getTabs().add(documentTab);
        
        final AliasQueryView queryView = new AliasQueryView(aliasController);
        final Tab queryTab = new Tab("Query");
        queryTab.setContent(queryView);
        details.getTabs().add(queryTab);
        
        details.getSelectionModel().selectedItemProperty().addListener((prop, oldSel, newSel) -> {
            if (newSel == detailsTab) {
                detailsView.refreshSettings();
            }
        });
        
        details.getSelectionModel().clearSelection();
        details.getSelectionModel().select(detailsTab);
        
        setContent(details);
        
        connectionSupplier = () -> aliasController.getAlias().getConnection();
    }
    
    @Override
    public ESConnection getConnection() {
        return connectionSupplier.get();
    }
    
}
