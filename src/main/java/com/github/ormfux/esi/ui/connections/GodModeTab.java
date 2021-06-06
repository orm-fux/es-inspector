package com.github.ormfux.esi.ui.connections;

import java.util.function.Supplier;

import com.github.ormfux.esi.controller.GodModeController;
import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.model.session.SessionGMTabData;
import com.github.ormfux.esi.model.session.SessionTabData;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.ui.ESConnectedView;
import com.github.ormfux.esi.ui.component.AsyncButton;
import com.github.ormfux.esi.ui.component.RestorableTab;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;
import com.github.ormfux.esi.ui.images.ImageKey;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class GodModeTab extends RestorableTab implements ESConnectedView {
    
    private final TextArea resultField = new SourceCodeTextArea();
    
    private final Text responseCodeText = new Text(" ");
    
    private final Text responseMessageText = new Text(" ");
    
    private final TextArea queryField = new SourceCodeTextArea();
    
    private final TextField endpointField = new TextField();
    
    private ComboBox<String> httpMethodBox;
    
    private final Supplier<ESConnection> connectionSupplier;
    
    public GodModeTab(final GodModeController godModeController) {
        setText(godModeController.getConnection().getName() + ": God Mode");
        
        final SplitPane content = new SplitPane();
        content.setPadding(new Insets(5));
        
        final VBox querySubView = createQueryView(godModeController);
        final VBox resultSubView = createResultSubView();
        
        content.setOrientation(Orientation.VERTICAL);
        content.setDividerPositions(0.5);
        content.getItems().addAll(new StackPane(querySubView), new StackPane(resultSubView));
        
        setContent(content);
        
        connectionSupplier = () -> godModeController.getConnection();
    }

    private VBox createResultSubView() {
        final VBox resultSubView = new VBox(2);
        resultSubView.setPadding(new Insets(2));
        
        final HBox resultStatusBar = new HBox(2, new Label("Response Status: "), responseCodeText, responseMessageText);
        
        resultField.setEditable(false);
        
        final ScrollPane resultContainer = new ScrollPane(resultField);
        resultContainer.setFitToHeight(true);
        resultContainer.setFitToWidth(true);
        VBox.setVgrow(resultContainer, Priority.ALWAYS);
        
        resultSubView.getChildren().addAll(resultStatusBar, resultContainer);
        return resultSubView;
    }
    
    private VBox createQueryView(final GodModeController godModeController) {
        final VBox querySubView = new VBox(2);
        querySubView.setPadding(new Insets(2));
        
        httpMethodBox = new ComboBox<>();
        httpMethodBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        httpMethodBox.getSelectionModel().select("GET");
        
        final Text baseUrlText = new Text(godModeController.getConnection().getUrl() + "/");
        
        HBox.setHgrow(endpointField, Priority.ALWAYS);
        
        final AsyncButton searchButton = new AsyncButton("Execute");
        final Node runningIcon = searchButton.getRunningIndicator();
        
        searchButton.disableProperty().bind(httpMethodBox.getSelectionModel().selectedItemProperty().isNull()
                                                .or(endpointField.textProperty().isEmpty())
                                                .or(runningIcon.visibleProperty()));
        searchButton.setAction(() -> {
                final ESResponse response = godModeController.executeRequest(httpMethodBox.getSelectionModel().getSelectedItem(), endpointField.getText(), queryField.getText());
                
                responseCodeText.setText(response.getResponseCode() + "");
                responseMessageText.setText(response.getResponseMessage());
                resultField.setText(response.getResponseBody());
            });
        
        final HBox actionsBar = new HBox(2, httpMethodBox, baseUrlText, endpointField, searchButton, runningIcon);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        
        final ScrollPane queryContainer = new ScrollPane(queryField);
        queryContainer.setFitToHeight(true);
        queryContainer.setFitToWidth(true);
        VBox.setVgrow(queryContainer, Priority.ALWAYS);
        
        querySubView.getChildren().addAll(actionsBar, queryContainer);
        
        return querySubView;
    }
    
    @Override
    public ESConnection getConnection() {
        return connectionSupplier.get();
    }
    
    @Override
    public SessionTabData getRestorableData() {
        final SessionGMTabData tabData = new SessionGMTabData();
        tabData.setConnectionId(getConnection().getId());
        tabData.setHttpMethod(httpMethodBox.getSelectionModel().getSelectedItem());
        tabData.setRequestBody(queryField.getText());
        tabData.setEndpoint(endpointField.getText());
        
        return tabData;
    }
    
    @Override
    public void fillWithRestoreData(final SessionTabData tabData) {
        setRestore(true);
        
        final SessionGMTabData restoredData = (SessionGMTabData) tabData;
        httpMethodBox.getSelectionModel().select(restoredData.getHttpMethod());
        queryField.setText(restoredData.getRequestBody());
        endpointField.setText(restoredData.getEndpoint());
    }
    
    @Override
    protected ImageKey getTabIconKey() {
        return ImageKey.LIGHTNING;
    }
}
