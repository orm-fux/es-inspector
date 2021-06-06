package com.github.ormfux.esi.ui.alias;

import java.util.function.Supplier;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.exception.ApplicationException;
import com.github.ormfux.esi.model.session.DetailsTab;
import com.github.ormfux.esi.model.session.SessionAliasDetailsTabData;
import com.github.ormfux.esi.model.session.SessionTabData;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.ui.ESConnectedView;
import com.github.ormfux.esi.ui.component.RestorableTab;
import com.github.ormfux.esi.ui.images.ImageKey;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

public class AliasDetailsTab extends RestorableTab implements ESConnectedView {
    
    private final Supplier<ESConnection> connectionSupplier;
    
    private final Supplier<String> aliasNameSupplier;
    
    private final AliasQueryView queryView;
    
    public AliasDetailsTab(final AliasDetailsController aliasController) {
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
        
        queryView = new AliasQueryView(aliasController);
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
        aliasNameSupplier = () -> aliasController.getAlias().getName();
    }
    
    @Override
    public ESConnection getConnection() {
        return connectionSupplier.get();
    }
    
    @Override
    public SessionTabData getRestorableData() {
        final SessionAliasDetailsTabData tabData = new SessionAliasDetailsTabData();
        tabData.setConnectionId(getConnection().getId());
        tabData.setAliasName(aliasNameSupplier.get());
        
        final TabPane contentTabs = (TabPane) getContent();
        final Tab selectedTab = contentTabs.getSelectionModel().getSelectedItem();
        
        if (selectedTab.getContent() instanceof AliasDetailsView) {
            tabData.setSelectedTab(DetailsTab.DETAILS);
        } else if (selectedTab.getContent() instanceof AliasDocumentView) {
            tabData.setSelectedTab(DetailsTab.DOCUMENT);
        } else if (selectedTab.getContent() instanceof AliasQueryView) {
            tabData.setSelectedTab(DetailsTab.QUERY);
        }
        
        tabData.setPlainQuery(queryView.getPlainQuery());
        
        return tabData;
    }
    
    @Override
    public void fillWithRestoreData(final SessionTabData tabData) {
        setRestore(true);
        
        final SessionAliasDetailsTabData restoreData = (SessionAliasDetailsTabData) tabData;
        final Class<?> selectedTabType;
        
        switch (restoreData.getSelectedTab()) {
            case DETAILS:
                selectedTabType = AliasDetailsView.class;
                break;
            case DOCUMENT:
                selectedTabType = AliasDocumentView.class;
                break;
            case QUERY:
                selectedTabType = AliasQueryView.class;
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
        return ImageKey.ALIAS;
    }
}
