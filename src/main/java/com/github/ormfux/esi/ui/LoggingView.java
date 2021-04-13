package com.github.ormfux.esi.ui;

import com.github.ormfux.esi.model.LogEntry;
import com.github.ormfux.esi.model.LogEntry.Level;
import com.github.ormfux.esi.service.LoggingService;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.Getter;

public class LoggingView extends BorderPane {
    
    private final DetailsDialog detailsDialog = new DetailsDialog();
    
    public LoggingView(final LoggingService loggingService) {
        final TableView<LogEntry> logEntryTable = new HeadlessAutoscrollTable<>(loggingService.getLogEntries());
        
        logEntryTable.setRowFactory( tv -> {
            TableRow<LogEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    final LogEntry logEntry = row.getItem();
                    detailsDialog.getTimestampText().setText(logEntry.getTimestamp());
                    detailsDialog.getLevelText().setText("" + logEntry.getLevel());
                    detailsDialog.getMessageArea().setText(logEntry.getMessage());
                    detailsDialog.getDetailsArea().setText(logEntry.getDetailsUnformatted());
                    detailsDialog.showAndWait();
                }
            });
            return row ;
        });
        
        final TableColumn<LogEntry, String> timestampColumn = new TableColumn<>();
        timestampColumn.setSortable(false);
        timestampColumn.setReorderable(false);
        timestampColumn.prefWidthProperty().bind(logEntryTable.widthProperty().multiply(0.13));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        logEntryTable.getColumns().add(timestampColumn);
        
        final TableColumn<LogEntry, Level> levelColumn = new TableColumn<>();
        levelColumn.setSortable(false);
        levelColumn.setReorderable(false);
        levelColumn.prefWidthProperty().bind(logEntryTable.widthProperty().multiply(0.05));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelColumn.setCellFactory(column -> {
            return new TableCell<>() {
                protected void updateItem(Level item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        final Text text = new Text(item.name());
                        
                        switch(item) {
                            case INFO:
                                text.setFill(Color.GREEN);
                                break;
                            case WARN:
                                text.setFill(Color.ORANGE);
                                break;
                            case ERROR:
                                text.setFill(Color.RED);
                                break;
                        }
                        setText(null);
                        setGraphic(text);
                    }
                }
            };
        });
        logEntryTable.getColumns().add(levelColumn);
        
        final TableColumn<LogEntry, String> messageColumn = new TableColumn<>();
        messageColumn.setSortable(false);
        messageColumn.setReorderable(false);
        messageColumn.prefWidthProperty().bind(logEntryTable.widthProperty().multiply(0.3));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        logEntryTable.getColumns().add(messageColumn);
        
        final TableColumn<LogEntry, String> detailsColumn = new TableColumn<>();
        detailsColumn.setSortable(false);
        detailsColumn.setReorderable(false);
        detailsColumn.prefWidthProperty().bind(logEntryTable.widthProperty().multiply(0.5));
        detailsColumn.setCellFactory(column -> {
            return new TableCell<>() {
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(null);

                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        final Label text = new Label(item.length() > 300 ? item.substring(0, 297) + "..." : item);
                        setGraphic(text);
                    }
                }
            };
        });
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        logEntryTable.getColumns().add(detailsColumn);
        
        setPadding(new Insets(5));
        setCenter(logEntryTable);
    }
    
    private static class HeadlessAutoscrollTable<T> extends TableView<T> {
        public HeadlessAutoscrollTable(final ObservableList<T> items) {
            super(items);
            getItems().addListener((ListChangeListener<T>) change -> {
                change.next();
                if (!getItems().isEmpty()) {
                    scrollTo(getItems().size() - 1);
                }
            });
        }
        
        @Override
        public void resize(double width, double height) {
            super.resize(width, height);
            Pane header = (Pane) lookup("TableHeaderRow");
            header.setMinHeight(10);
            header.setPrefHeight(10);
            header.setMaxHeight(10);
            //header.setVisible(false);
        }
    }
    
    @Getter
    private static class DetailsDialog extends Dialog<Void> {
        
        private final Text timestampText = new Text();
        
        private final Text levelText = new Text();
        
        private final TextArea messageArea = new TextArea();
        
        private final TextArea detailsArea = new TextArea(); 
        
        public DetailsDialog() {
            setTitle("Details");
            setResizable(true);
            
            final DialogPane dialogPane = getDialogPane();
            dialogPane.setMinWidth(550);
            dialogPane.setMinHeight(300);
            
            messageArea.setEditable(false);
            messageArea.setPrefRowCount(3);
            detailsArea.setEditable(false);
            
            dialogPane.setContent(createContentGrid());
            dialogPane.getButtonTypes().addAll(ButtonType.OK);
            
            final Button okButton = (Button)dialogPane.lookupButton(ButtonType.OK);
            okButton.setText("OK");
        }
        
        private Node createContentGrid() {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(3);
            grid.setPrefWidth(300.0);
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setAlignment(Pos.CENTER_LEFT);
            
            grid.addRow(0, new Text("Timestamp:"), timestampText);
            grid.addRow(1, new Text("Level"), levelText);
            grid.addRow(2, new Text("Message"), messageArea);
            grid.addRow(3, new Text("Details"), detailsArea);
            
            return grid;
        }
    }
    
}
