package com.github.ormfux.esi;

import static com.github.ormfux.simple.di.InjectionContext.getBean;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.controller.GodModeController;
import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.controller.ManageAliasesController;
import com.github.ormfux.esi.controller.ManageConnectionsController;
import com.github.ormfux.esi.controller.ManageIndicesController;
import com.github.ormfux.esi.service.ESConnectionUsageStatusService;
import com.github.ormfux.esi.service.LoggingService;
import com.github.ormfux.esi.ui.ConnectionSelectionView;
import com.github.ormfux.esi.ui.LoggingView;
import com.github.ormfux.esi.ui.MainLayout;
import com.github.ormfux.esi.ui.alias.AliasDetailsTab;
import com.github.ormfux.esi.ui.connections.GodModeTab;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.images.ImageRegistry;
import com.github.ormfux.esi.ui.index.IndexDetailsTab;
import com.github.ormfux.esi.ui.index.IndexDetailsTabPane;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

//TODO https://gist.github.com/jewelsea/1463485
//TODO https://github.com/afester/RichTextFX
public class ESInspectorApplication extends Application {

    public static void start(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(ImageRegistry.getImage(ImageKey.APPLICATION));
        primaryStage.setTitle("Elasticsearch Inspector");
        
        final LoggingService loggingService = getBean(LoggingService.class);
        
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> loggingService.addLogEntry(exception));
        
        final ESConnectionUsageStatusService connectionStatusService = getBean(ESConnectionUsageStatusService.class);
        
        final ManageConnectionsController manageConnectionsController = getBean(ManageConnectionsController.class);
        final ManageIndicesController manageIndicesController = getBean(ManageIndicesController.class);
        final ManageAliasesController manageAliasesController = getBean(ManageAliasesController.class);
        
        final IndexDetailsTabPane indexTabs = new IndexDetailsTabPane();
        
        final ConnectionSelectionView connectionsListView = new ConnectionSelectionView(manageConnectionsController, manageIndicesController, manageAliasesController);
        manageConnectionsController.setGodModeViewOpener(connection -> {
            connectionStatusService.connectionOpened(connection.getId());
            
            final GodModeController godModeController = getBean(GodModeController.class);
            godModeController.setConnection(connection);
            
            final GodModeTab tab = new GodModeTab(godModeController);
            tab.setOnClosed(e -> connectionStatusService.connectionClosed(connection.getId()));
            indexTabs.getTabs().add(tab);
            indexTabs.getSelectionModel().select(tab);
        });
        
        
        manageIndicesController.setDetailsViewOpener(index -> {
            connectionStatusService.connectionOpened(index.getConnection().getId());
            
            final IndexDetailsController detailsController = getBean(IndexDetailsController.class);
            detailsController.setIndex(index);
            
            final IndexDetailsTab tab = new IndexDetailsTab(detailsController);
            tab.setOnClosed(e -> connectionStatusService.connectionClosed(index.getConnection().getId()));
            indexTabs.getTabs().add(tab);
            indexTabs.getSelectionModel().select(tab);
            
        });
        
        manageAliasesController.setDetailsViewOpener(alias -> {
            connectionStatusService.connectionOpened(alias.getConnection().getId());
            
            final AliasDetailsController detailsController = getBean(AliasDetailsController.class);
            detailsController.setAlias(alias);
            
            final AliasDetailsTab tab = new AliasDetailsTab(detailsController);
            tab.setOnClosed(e -> connectionStatusService.connectionClosed(alias.getConnection().getId()));
            indexTabs.getTabs().add(tab);
            indexTabs.getSelectionModel().select(tab);
        });
        
        final LoggingView loggingView = new LoggingView(loggingService);
        
        final Scene scene = new Scene(new MainLayout(connectionsListView, indexTabs, loggingView), 1280, 720);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
