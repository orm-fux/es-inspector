package com.github.ormfux.esi;

import static com.github.ormfux.simple.di.InjectionContext.getBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.controller.GodModeController;
import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.controller.InspectorSessionController;
import com.github.ormfux.esi.controller.ManageAliasesController;
import com.github.ormfux.esi.controller.ManageConnectionsController;
import com.github.ormfux.esi.controller.ManageIndicesController;
import com.github.ormfux.esi.model.LogEntry.Level;
import com.github.ormfux.esi.model.alias.ESMultiIndexAlias;
import com.github.ormfux.esi.model.index.ESIndex;
import com.github.ormfux.esi.model.session.InspectorSession;
import com.github.ormfux.esi.model.session.SessionAliasDetailsTabData;
import com.github.ormfux.esi.model.session.SessionGMTabData;
import com.github.ormfux.esi.model.session.SessionIndexDetailsTabData;
import com.github.ormfux.esi.model.session.SessionTabData;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.service.ESConnectionUsageStatusService;
import com.github.ormfux.esi.service.LoggingService;
import com.github.ormfux.esi.ui.ConnectionSelectionView;
import com.github.ormfux.esi.ui.ESConnectedView;
import com.github.ormfux.esi.ui.LoggingView;
import com.github.ormfux.esi.ui.MainLayout;
import com.github.ormfux.esi.ui.alias.AliasDetailsTab;
import com.github.ormfux.esi.ui.component.RestorableTab;
import com.github.ormfux.esi.ui.connections.GodModeTab;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.images.ImageRegistry;
import com.github.ormfux.esi.ui.index.IndexDetailsTab;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class ESInspectorApplication extends Application {

    private final ESConnectionUsageStatusService connectionStatusService = getBean(ESConnectionUsageStatusService.class);
    
    private final ManageConnectionsController manageConnectionsController = getBean(ManageConnectionsController.class);
    
    private final ManageIndicesController manageIndicesController = getBean(ManageIndicesController.class);
    
    private final ManageAliasesController manageAliasesController = getBean(ManageAliasesController.class);
    
    private final InspectorSessionController sessionService = getBean(InspectorSessionController.class);
    
    private final LoggingService loggingService = getBean(LoggingService.class);
    
    private final TabPane mainTabs = new TabPane();
    
    public static void start(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(ImageRegistry.getImage(ImageKey.APPLICATION));
        primaryStage.setTitle("Elasticsearch Inspector");
        
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> loggingService.addLogEntry(exception));
        
        initMainControllers();
        
        final ConnectionSelectionView connectionsListView = new ConnectionSelectionView(manageConnectionsController, manageIndicesController, manageAliasesController);
        
        manageConnectionsController.setGodModeViewOpener(this::openGodModeTab);
        manageIndicesController.setDetailsViewOpener(this::openIndexDetailsTab);
        manageAliasesController.setDetailsViewOpener(this::openAliasDetailsTab);
        
        final LoggingView loggingView = new LoggingView(loggingService);
        
        final Scene scene = new Scene(new MainLayout(connectionsListView, mainTabs, loggingView), 1280, 720);
        scene.getStylesheets().add(ESInspectorApplication.class.getResource("ui/es-inspector.css").toExternalForm());
        
        primaryStage.setOnShown(e -> restorePreviousSession(scene.getWindow()));
        
        primaryStage.setOnCloseRequest(e -> {
            saveCurrentSession();
            System.exit(1);
        });
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void saveCurrentSession() {
        loggingService.addLogEntry(Level.INFO, "Saving current session.");
        final InspectorSession currentSession = new InspectorSession();
        
        mainTabs.getTabs()
                .stream()
                .filter(tab -> tab instanceof RestorableTab)
                .map(tab -> (RestorableTab) tab)
                .filter(RestorableTab::isRestore)
                .map(RestorableTab::getRestorableData)
                .forEach(currentSession::addTabData);
        
        sessionService.saveSession(currentSession);
    }
    
    private void restorePreviousSession(final Window parentWindow) {
        loggingService.addLogEntry(Level.INFO, "Restoring previous session.");
        final InspectorSession session = sessionService.loadSession();
        
        if (session.getTabData() != null && !session.getTabData().isEmpty()) {
            final Stage progressDialog = new Stage(StageStyle.UNDECORATED);
            progressDialog.initModality(Modality.WINDOW_MODAL);
            progressDialog.setResizable(false);
            progressDialog.initOwner(parentWindow);
            
            final Task<Void> restoreTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int progressCount = 0;
                    updateProgress(progressCount++, session.getTabData().size());
                    
                    for (final SessionTabData tabData : session.getTabData()) {
                        final CountDownLatch doneLatch = new CountDownLatch(1);
                        updateProgress(progressCount++, session.getTabData().size());
                        
                        Platform.runLater(() -> {
                            try {
                                restoreTab(tabData);
                            } finally {
                                doneLatch.countDown();
                            }
                        });
                        
                        doneLatch.await();
                    }
                    
                    return null;
                }
            };
            
            restoreTask.stateProperty().addListener((prop, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED || newState == Worker.State.CANCELLED || newState == Worker.State.FAILED) {
                    progressDialog.close();
                }
            });
            
            restoreTask.exceptionProperty().addListener((prop, oldState, newState) -> {
                loggingService.addLogEntry(newState);
            });
            
            final Text restoringLabel = new Text("Restoring pinned tabs");
            final ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(restoreTask.progressProperty());
            
            final VBox progressDialogContent = new VBox(3);
            progressDialogContent.setAlignment(Pos.CENTER);
            progressDialogContent.getChildren().addAll(restoringLabel, progressBar);
            
            progressDialog.setScene(new Scene(progressDialogContent));
            progressDialog.show();
            
            new Thread(restoreTask).start();
        }
    }

    private void restoreTab(final SessionTabData tabData) {
        if (tabData instanceof SessionAliasDetailsTabData) {
            final SessionAliasDetailsTabData aliasTabData = (SessionAliasDetailsTabData) tabData;
            final ESMultiIndexAlias alias = sessionService.lookupAlias(aliasTabData);
            
            if (alias != null) {
                final AliasDetailsTab tab = openAliasDetailsTab(alias);
                tab.fillWithRestoreData(aliasTabData);
            } else {
                loggingService.addLogEntry(Level.WARN, "Could not find connection or alias for alias name: " + aliasTabData.getAliasName());
            }
            
        } else if (tabData instanceof SessionIndexDetailsTabData) {
            final SessionIndexDetailsTabData indexTabData = (SessionIndexDetailsTabData) tabData;
            final ESIndex index = sessionService.lookupIndex(indexTabData);
            
            if (index != null) {
                final IndexDetailsTab tab = openIndexDetailsTab(index);
                tab.fillWithRestoreData(indexTabData);
            } else {
                loggingService.addLogEntry(Level.WARN, "Could not find connection or index for index name: " + indexTabData.getIndexName());
            }

            
        } else if (tabData instanceof SessionGMTabData) {
            final SessionGMTabData godModeTabData = (SessionGMTabData) tabData;
            final ESConnection connection = sessionService.lookupConnection(godModeTabData);
            
            if (connection != null) {
                final GodModeTab tab = openGodModeTab(connection);
                tab.fillWithRestoreData(godModeTabData);
            } else {
                loggingService.addLogEntry(Level.WARN, "Could not find connection for god mode tab.");
            }
        }
    }

    private AliasDetailsTab openAliasDetailsTab(final ESMultiIndexAlias alias) {
        connectionStatusService.connectionOpened(alias.getConnection().getId());
        
        final AliasDetailsController detailsController = getBean(AliasDetailsController.class);
        detailsController.setAlias(alias);
        
        final AliasDetailsTab tab = new AliasDetailsTab(detailsController);
        tab.setOnClosed(e -> connectionStatusService.connectionClosed(alias.getConnection().getId()));
        mainTabs.getTabs().add(tab);
        mainTabs.getSelectionModel().select(tab);
        
        return tab;
    }

    private IndexDetailsTab openIndexDetailsTab(final ESIndex index) {
        connectionStatusService.connectionOpened(index.getConnection().getId());
        
        final IndexDetailsController detailsController = getBean(IndexDetailsController.class);
        detailsController.setIndex(index);
        
        final IndexDetailsTab tab = new IndexDetailsTab(detailsController);
        tab.setOnClosed(e -> connectionStatusService.connectionClosed(index.getConnection().getId()));
        mainTabs.getTabs().add(tab);
        mainTabs.getSelectionModel().select(tab);
        
        return tab;
    }

    private GodModeTab openGodModeTab(final ESConnection connection) {
        connectionStatusService.connectionOpened(connection.getId());
        
        final GodModeController godModeController = getBean(GodModeController.class);
        godModeController.setConnection(connection);
        
        final GodModeTab tab = new GodModeTab(godModeController);
        tab.setOnClosed(e -> connectionStatusService.connectionClosed(connection.getId()));
        mainTabs.getTabs().add(tab);
        mainTabs.getSelectionModel().select(tab);
        
        return tab;
    }

    private void initMainControllers() {
        manageConnectionsController.addCloseConnectionHandler(connection -> {
            final List<Tab> tabsToClose = mainTabs.getTabs()
                                                   .stream()
                                                   .filter(tab -> tab instanceof ESConnectedView)
                                                   .filter(tab -> ((ESConnectedView) tab).getConnection().getId().equals(connection.getId()))
                                                   .collect(Collectors.toList());
            
            tabsToClose.forEach(tab -> {
                         mainTabs.getTabs().remove(tab);
                         connectionStatusService.connectionClosed(((ESConnectedView) tab).getConnection().getId());
                     });
        });
        
        manageConnectionsController.addCloseConnectionHandler(connection -> {
            if (manageIndicesController.getSelectedConnection().get() != null && connection.getId().equals(manageIndicesController.getSelectedConnection().get().getId())) {
                manageIndicesController.getSelectedConnection().set(null);
            }
        });
        
        manageConnectionsController.addCloseConnectionHandler(connection -> {
            if (manageAliasesController.getSelectedConnection().get() != null && connection.getId().equals(manageAliasesController.getSelectedConnection().get().getId())) {
                manageAliasesController.getSelectedConnection().set(null);
            }
        });
    }
}
