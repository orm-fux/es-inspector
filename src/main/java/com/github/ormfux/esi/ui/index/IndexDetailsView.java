package com.github.ormfux.esi.ui.index;

import static java.lang.Long.parseLong;
import static java.util.stream.Collectors.joining;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Supplier;

import com.github.ormfux.esi.controller.IndexDetailsController;
import com.github.ormfux.esi.model.index.ESIndexSettings;
import com.github.ormfux.esi.model.index.mapping.ESIndexMappingProperty;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class IndexDetailsView extends ScrollPane {

    private final TextField connectionText;
    private final TextField nameText;
    private final TextField urlText;
    private final TextField docsCountText;
    private final TextField storageSizeText;
    
    private final TextField replicasText;
    private final TextField shardsText;
    private final TextField healthText;
    private final TextField aliasesText;
    private final TextField creationDateText;
    
    private final TextField versionText;
    private final TextArea rawSettingsText = new TextArea();
    
    private final TableView<ESIndexMappingProperty> mappingPropertiesTable;
    
    private final Supplier<IndexDetailsController> dataSupplier;
    
    public IndexDetailsView(final IndexDetailsController indexController) {
        setPadding(new Insets(5));
        setFitToHeight(true);
        setFitToWidth(true);
        
        dataSupplier = () -> indexController;
        
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
        
        final Label aliasesLabel = createLabel("Aliases");
        aliasesText = createTextField();
        detailsContainer.addRow(5, aliasesLabel, aliasesText);
        
        final Label creationDateLabel = createLabel("Creation Date");
        creationDateText = createTextField();
        detailsContainer.addRow(6, creationDateLabel, creationDateText);
        
        final Label healthLabel = createLabel("Health");
        healthText = createTextField();
        detailsContainer.addRow(7, healthLabel, healthText);
        
        final Label docsCountLabel = createLabel("Documents");
        docsCountText = createTextField();
        detailsContainer.addRow(8, docsCountLabel, docsCountText);
        
        final Label storageSizeLabel = createLabel("Document Size");
        storageSizeText = createTextField();
        detailsContainer.addRow(9, storageSizeLabel, storageSizeText);
        
        final Label shardsLabel = createLabel("Shards");
        shardsText = createTextField();
        detailsContainer.addRow(10, shardsLabel, shardsText);
        
        final Label replicasLabel = createLabel("Replicas");
        replicasText = createTextField();
        detailsContainer.addRow(11, replicasLabel, replicasText);
        
        rawSettingsText.setFont(Font.font("Courier New"));
        rawSettingsText.setEditable(false);
        rawSettingsText.setPrefColumnCount(100);
        rawSettingsText.setPrefRowCount(40);
        final Alert rawSettingsDialog = new Alert(AlertType.INFORMATION);
        rawSettingsDialog.setHeaderText(null);
        rawSettingsDialog.setGraphic(null);
        rawSettingsDialog.getDialogPane().setContent(rawSettingsText);
        
        final Label rawSettingsLabel = createLabel("Settings");
        final Button rawSettingsButton = new Button("Show Raw");
        rawSettingsButton.setOnAction(e -> rawSettingsDialog.show());
        detailsContainer.addRow(12, rawSettingsLabel, rawSettingsButton);
        
        final Separator separator2 = new Separator(Orientation.HORIZONTAL);
        GridPane.setColumnSpan(separator2, 2);
        detailsContainer.addRow(13, separator2);
        
        final Label mappingLabel = createLabel("Mapping");
        GridPane.setValignment(mappingLabel, VPos.TOP);
        mappingPropertiesTable = createMappingTable();
        GridPane.setHgrow(mappingPropertiesTable, Priority.ALWAYS);
        GridPane.setVgrow(mappingPropertiesTable, Priority.NEVER);
        detailsContainer.addRow(14, mappingLabel, mappingPropertiesTable);
        
        setContent(detailsContainer);
        //getChildren().addAll(detailsContainer);
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
    
    private TableView<ESIndexMappingProperty> createMappingTable() {
        final TableView<ESIndexMappingProperty> mappingTable = new TableView<>();
        mappingTable.setFixedCellSize(25);
        mappingTable.prefHeightProperty().bind(mappingTable.fixedCellSizeProperty().multiply(Bindings.size(mappingTable.getItems()).add(1.01)));
        mappingTable.minHeightProperty().bind(mappingTable.prefHeightProperty());
        mappingTable.maxHeightProperty().bind(mappingTable.prefHeightProperty());
        
        final TableColumn<ESIndexMappingProperty, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        mappingTable.getColumns().add(pathColumn);
        
        final TableColumn<ESIndexMappingProperty, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        mappingTable.getColumns().add(typeColumn);
        
        final TableColumn<ESIndexMappingProperty, String> analyzerColumn = new TableColumn<>("Analyzer");
        analyzerColumn.setCellValueFactory(new PropertyValueFactory<>("analyzer"));
        mappingTable.getColumns().add(analyzerColumn);
        
        final TableColumn<ESIndexMappingProperty, String> searchAnalyzerColumn = new TableColumn<>("Search Analyzer");
        searchAnalyzerColumn.setCellValueFactory(new PropertyValueFactory<>("searchAnalyzer"));
        mappingTable.getColumns().add(searchAnalyzerColumn);
        
        return mappingTable;
    }
    
    public void refreshSettings() {
        final IndexDetailsController data = dataSupplier.get();
        
        connectionText.setText(data.getIndex().getConnection().getName());
        nameText.setText(data.getIndex().getName());
        urlText.setText(data.getIndex().getConnection().getUrl() + "/" + data.getIndex().getName());
        storageSizeText.setText(data.getIndex().getStoreSize());
        docsCountText.setText(data.getIndex().getDocsCount());
        
        healthText.setText(data.getIndex().getHealth());
        aliasesText.setText(data.lookupIndexAliasNames().stream().collect(joining(", ")));
        versionText.setText(data.lookupElasticsearchVersion());
        
        final ESIndexSettings settings = data.lookupIndexSettings();
        
        if (settings != null) {
            shardsText.setText(settings.getShards());
            replicasText.setText(settings.getReplicas());
            creationDateText.setText(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").format(new Date(parseLong(settings.getCreationDate()))));
            rawSettingsText.setText(settings.getRawSettings());
        }
        
        final List<ESIndexMappingProperty> mappings = data.lookupIndexMappings();
        mappingPropertiesTable.getItems().setAll(mappings);
    }
    
}
