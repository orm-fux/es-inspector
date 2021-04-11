package com.github.ormfux.esi.ui.alias;

import static java.util.stream.Collectors.joining;

import java.util.function.Supplier;

import com.github.ormfux.esi.controller.AliasDetailsController;
import com.github.ormfux.esi.model.alias.ESMultiIndexAlias;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class AliasDetailsView extends ScrollPane {

    private final TextField connectionText;
    private final TextField urlText;
    private final TextField versionText;
    
    private final TextField nameText;
    private final TextField indicesText;
    private final TextField writeIndexText;
    
    private final Supplier<AliasDetailsController> dataSupplier;
    
    public AliasDetailsView(final AliasDetailsController aliasController) {
        setPadding(new Insets(5));
        setFitToHeight(true);
        setFitToWidth(true);
        
        dataSupplier = () -> aliasController;
        
        final GridPane detailsContainer = new GridPane();
        detailsContainer.setPadding(new Insets(5));
        detailsContainer.setHgap(20);
        detailsContainer.setVgap(10);
        
        final Label connectionLabel = createLabel("Connection");
        connectionText = createTextField();
        detailsContainer.addRow(0, connectionLabel, connectionText);
        
        final Label urlLabel = createLabel("URL");
        urlText = createTextField();
        detailsContainer.addRow(1, urlLabel, urlText);
        
        final Label versionLabel = createLabel("Version");
        versionText = createTextField();
        detailsContainer.addRow(2, versionLabel, versionText);
        
        final Separator separator = new Separator(Orientation.HORIZONTAL);
        GridPane.setColumnSpan(separator, 2);
        detailsContainer.addRow(3, separator);
        
        final Label nameLabel = createLabel("Name");
        nameText = createTextField();
        detailsContainer.addRow(4, nameLabel, nameText);
        
        final Label indicesLabel = createLabel("Indices");
        indicesText = createTextField();
        detailsContainer.addRow(5, indicesLabel, indicesText);
        
        final Label writeIndexLabel = createLabel("Write Index");
        writeIndexText = createTextField();
        detailsContainer.addRow(6, writeIndexLabel, writeIndexText);
        
        setContent(detailsContainer);
    }

    private Label createLabel(final String text) {
        final Label connectionLabel = new Label(text);
        connectionLabel.setStyle("-fx-font-weight: bold");
        
        return connectionLabel;
    }
    
    private TextField createTextField() {
        final TextField tf = new TextField();
        tf.setMinWidth(Region.USE_PREF_SIZE);
        tf.setMaxWidth(Region.USE_PREF_SIZE);
        
        tf.setEditable(false);
        tf.setPadding(new Insets(0));
        tf.setStyle("-fx-opacity: 1; -fx-background-color: transparent");
        
        tf.textProperty().addListener((ov, prevText, currText) -> {
            // Do this in a Platform.runLater because of Textfield has no padding at first time and so on
            Platform.runLater(() -> {
                Text t = new Text(currText);
                t.setFont(tf.getFont()); // Set the same font, so the size is the same
                double width = t.getLayoutBounds().getWidth() // This big is the Text in the TextField
                        + tf.getPadding().getLeft() + tf.getPadding().getRight() // Add the padding of the TextField
                        + 2d; // Add some spacing
                tf.setPrefWidth(width); // Set the width
                tf.positionCaret(tf.getCaretPosition()); // If you remove this line, it flashes a little bit
            });
        });
        
        return tf;
    }
    
    public void refreshSettings() {
        final AliasDetailsController data = dataSupplier.get();
        final ESMultiIndexAlias alias = data.getAlias();
        
        connectionText.setText(alias.getConnection().getName());
        urlText.setText(alias.getConnection().getUrl() + "/" + alias.getName());
        versionText.setText(data.lookupElasticsearchVersion());
        
        nameText.setText(alias.getName());
        indicesText.setText(alias.getIndices().stream().collect(joining(", ")));
        writeIndexText.setText(alias.getWriteIndex());
    }
    
}
