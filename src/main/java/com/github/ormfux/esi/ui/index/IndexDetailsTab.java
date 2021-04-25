package com.github.ormfux.esi.ui.index;

import java.util.function.Supplier;

import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.ui.ESConnectedView;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.images.ImageRegistry;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.ImageView;

public class IndexDetailsTab extends Tab implements ESConnectedView {
    
    private final Supplier<ESConnection> connectionSupplier;
    
    public IndexDetailsTab(final IndexDetailsController indexController) {
        setClosable(true);
        
        final ImageView tabIcon = new ImageView(ImageRegistry.getImage(ImageKey.INDEX));
        tabIcon.setFitHeight(23);
        tabIcon.setFitWidth(23);
        setGraphic(tabIcon);
        
        setText(indexController.getIndex().getConnection().getName() + ": " + indexController.getIndex().getName());
        
        final TabPane details = new TabPane();
        details.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        details.setSide(Side.LEFT);
        
        final IndexDetailsView detailsView = new IndexDetailsView(indexController);
        final Tab detailsTab = new Tab("Details");
        detailsTab.setContent(detailsView);
        details.getTabs().add(detailsTab);
        
        final IndexDocumentView documentView = new IndexDocumentView(indexController);
        final Tab documentTab = new Tab("Document");
        documentTab.setContent(documentView);
        details.getTabs().add(documentTab);
        
        final IndexQueryView queryView = new IndexQueryView(indexController);
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
        
        connectionSupplier = () -> indexController.getIndex().getConnection();
    }
    
    @Override
    public ESConnection getConnection() {
        return connectionSupplier.get();
    }
    
}
