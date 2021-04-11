package com.github.ormfux.esi.ui.connections;

import com.github.ormfux.esi.controller.GodModeController;
import com.github.ormfux.esi.model.ESResponse;
import com.github.ormfux.esi.ui.component.SourceCodeTextArea;
import com.github.ormfux.esi.ui.images.ImageKey;
import com.github.ormfux.esi.ui.images.ImageRegistry;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GodModeTab extends Tab {
    
    private final TextArea resultField = new TextArea();
    
    private final Text responseCodeText = new Text(" ");
    
    private final Text responseMessageText = new Text(" ");
    
    public GodModeTab(final GodModeController godModeController) {
        setClosable(true);
        
        final ImageView tabIcon = new ImageView(ImageRegistry.getImage(ImageKey.LIGHTNING));
        tabIcon.setFitHeight(23);
        tabIcon.setFitWidth(23);
        setGraphic(tabIcon);
        
        setText(godModeController.getConnection().getName() + ": God Mode");
        
        final SplitPane content = new SplitPane();
        content.setPadding(new Insets(5));
        
        final VBox querySubView = createQueryView(godModeController);
        final VBox resultSubView = createResultSubView();
        
        content.setOrientation(Orientation.VERTICAL);
        content.setDividerPositions(0.5);
        content.getItems().addAll(new StackPane(querySubView), new StackPane(resultSubView));
        
        setContent(content);
    }

    private VBox createResultSubView() {
        final VBox resultSubView = new VBox(2);
        resultSubView.setPadding(new Insets(2));
        
        final HBox resultStatusBar = new HBox(2, new Label("Response Status: "), responseCodeText, responseMessageText);
        
        resultField.setFont(Font.font("Courier New"));
        resultField.setEditable(false);
        
        final ScrollPane resultContainer = new ScrollPane(resultField);
        resultContainer.setFitToHeight(true);
        resultContainer.setFitToWidth(true);
        VBox.setVgrow(resultContainer, Priority.ALWAYS);
        
        resultSubView.getChildren().addAll(resultStatusBar, resultContainer);
        return resultSubView;
    }
    
    private VBox createQueryView(final GodModeController godModeController) {
        final TextArea queryField = new SourceCodeTextArea();
        
        final VBox querySubView = new VBox(2);
        querySubView.setPadding(new Insets(2));
        
        final ComboBox<String> httpMethodBox = new ComboBox<>();
        httpMethodBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        httpMethodBox.getSelectionModel().select("GET");
        
        final Text baseUrlText = new Text(godModeController.getConnection().getUrl() + "/");
        
        final TextField endpointField = new TextField();
        HBox.setHgrow(endpointField, Priority.ALWAYS);
        
        final Button searchButton = new Button("Execute");
        searchButton.disableProperty().bind(httpMethodBox.getSelectionModel().selectedItemProperty().isNull()
                                                .or(endpointField.textProperty().isEmpty()));
        searchButton.setOnAction(e -> {
            final ESResponse response = godModeController.executeRequest(httpMethodBox.getSelectionModel().getSelectedItem(), endpointField.getText(), queryField.getText());
            
            responseCodeText.setText(response.getResponseCode() + "");
            responseMessageText.setText(response.getResponseMessage());
            resultField.setText(response.getResponseBody());
        });
        
        final HBox actionsBar = new HBox(2, httpMethodBox, baseUrlText, endpointField, searchButton);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        
        final ScrollPane queryContainer = new ScrollPane(queryField);
        queryContainer.setFitToHeight(true);
        queryContainer.setFitToWidth(true);
        VBox.setVgrow(queryContainer, Priority.ALWAYS);
        
        querySubView.getChildren().addAll(actionsBar, queryContainer);
        
        return querySubView;
    }
}
