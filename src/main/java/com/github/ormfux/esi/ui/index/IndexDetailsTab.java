package com.github.ormfux.esi.ui.index;

import java.util.function.Supplier;

import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.model.session.DetailsTab;
import com.github.ormfux.esi.model.session.SessionIndexDetailsTabData;
import com.github.ormfux.esi.model.session.SessionTabData;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.ui.ESConnectedView;
import com.github.ormfux.esi.ui.component.RestorableTab;
import com.github.ormfux.esi.ui.images.ImageKey;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

public class IndexDetailsTab extends RestorableTab implements ESConnectedView {
    
    private final Supplier<ESConnection> connectionSupplier;
    
    private final Supplier<String> indexNameSupplier;
    
    private final IndexQueryView queryView;
    
    public IndexDetailsTab(final IndexDetailsController indexController) {
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
        
        queryView = new IndexQueryView(indexController);
        final Tab queryTab = new Tab("Query");
        queryTab.setContent(queryView);
        details.getTabs().add(queryTab);
        
        details.getSelectionModel().clearSelection();
        details.getSelectionModel().select(detailsTab);
        
        setContent(details);
        
        connectionSupplier = () -> indexController.getIndex().getConnection();
        indexNameSupplier = () -> indexController.getIndex().getName();
    }
    
    @Override
    public ESConnection getConnection() {
        return connectionSupplier.get();
    }
    
    @Override
    public SessionTabData getRestorableData() {
        final SessionIndexDetailsTabData tabData = new SessionIndexDetailsTabData();
        tabData.setConnectionId(getConnection().getId());
        tabData.setIndexName(indexNameSupplier.get());
        
        final TabPane contentTabs = (TabPane) getContent();
        final Tab selectedTab = contentTabs.getSelectionModel().getSelectedItem();
        
        if (selectedTab.getContent() instanceof IndexDetailsView) {
            tabData.setSelectedTab(DetailsTab.DETAILS);
        } else if (selectedTab.getContent() instanceof IndexDocumentView) {
            tabData.setSelectedTab(DetailsTab.DOCUMENT);
        } else if (selectedTab.getContent() instanceof IndexQueryView) {
            tabData.setSelectedTab(DetailsTab.QUERY);
        }
        
        tabData.setPlainQuery(queryView.getPlainQuery());
        tabData.setGuidedBooleanQuery(queryView.getGuidedBooleanQuery());
        tabData.setSelectedQueryType(queryView.getSelectedQueryType());
        
        return tabData;
    }
    
    @Override
    public void fillWithRestoreData(final SessionTabData tabData) {
        setRestore(true);
        
        final SessionIndexDetailsTabData restoreData = (SessionIndexDetailsTabData) tabData;
        final Class<?> selectedTabType;
        
        switch (restoreData.getSelectedTab()) {
            case DETAILS:
                selectedTabType = IndexDetailsView.class;
                break;
            case DOCUMENT:
                selectedTabType = IndexDocumentView.class;
                break;
            case QUERY:
                selectedTabType = IndexQueryView.class;
                break;
            default:
                throw new ApplicationException("Unsupported selected tab type: " + restoreData.getSelectedTab(), null);
        }
        
        final TabPane contentTabs = (TabPane) getContent();
        contentTabs.getTabs()
                   .stream()
                   .filter(tab -> selectedTabType.isAssignableFrom(tab.getContent().getClass()))
                   .findFirst()
                   .ifPresent(tab -> contentTabs.getSelectionModel().select(tab));
        
        queryView.fillWithData(restoreData);
    }
    
    @Override
    protected ImageKey getTabIconKey() {
        return ImageKey.INDEX;
    }
    
}
