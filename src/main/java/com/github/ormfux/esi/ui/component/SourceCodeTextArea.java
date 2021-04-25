package com.github.ormfux.esi.ui.component;

import java.util.Set;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SourceCodeTextArea extends TextArea {
    
    private static final Set<Character> INDENT_CHARACTERS = Set.of('{', '[');
    
    private static final Set<Character> OUTDENT_CHARACTERS = Set.of(']', '}');
    
    public SourceCodeTextArea() {
        setFont(Font.font("Courier New"));
        
        //quick'n'dirty auto-indent
        addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if(new KeyCodeCombination(KeyCode.ENTER).match(e)) {
                final String queryText = getText();
                int whiteSpaceCount = 0;
                
                for (int i = getCaretPosition() - 2; i >= 0; i--) {
                    final char character = queryText.charAt(i);
                    
                    if (character == '\n' || character == '\r') {
                        break;
                    } else if (Character.isWhitespace(character)) {
                        whiteSpaceCount++;
                    } else {
                        whiteSpaceCount = 0;
                    }
                }
                
                if (getCaretPosition() > 1) {
                    final char previousCharacter = queryText.charAt(getCaretPosition() - 2);
                    
                    if (INDENT_CHARACTERS.contains(previousCharacter)) {
                        whiteSpaceCount += 2;
                    }
                }
                
                for (int i = 0; i < whiteSpaceCount; i++) {
                    insertText(getCaretPosition(), " ");
                }
            }
        });
        
        addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (e.getCharacter() != null && e.getCharacter().length() == 1 && OUTDENT_CHARACTERS.contains(e.getCharacter().charAt(0))) {
                final String queryText = getText();
                boolean onlyWhitespaces = true;
                
                for (int i = getCaretPosition() - 1; i >= 0; i--) {
                    final char character = queryText.charAt(i);
                    
                    if (character == '\n' || character == '\r') {
                        break;
                    } else if (!Character.isWhitespace(character)) {
                        onlyWhitespaces = false;
                        break;
                    }
                }
                
                if (onlyWhitespaces) {
                    deleteText(getCaretPosition() - 2, getCaretPosition());
                }
                
            }
        });
        
        //i don't like tabs for indenting. use spaces
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (new KeyCodeCombination(KeyCode.TAB).match(e)) {
                insertText(getCaretPosition(), "  ");
                e.consume();
            } else if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(e)) {
                new SearchDialog().show();
            }
        });
    }
    
    private class SearchDialog extends Stage {
        
        private final TextField searchField = new TextField();
        
        private final Text notFoundText = new Text("Not Found");
        
        public SearchDialog() {
            setTitle("Search");
            setResizable(false);
            initOwner(SourceCodeTextArea.this.getScene().getWindow());
            initModality(Modality.WINDOW_MODAL);
            initStyle(StageStyle.UTILITY);
            
            final VBox content = new VBox(2);
            
            final HBox searchFieldContainer = new HBox(2);
            HBox.setHgrow(searchField, Priority.ALWAYS);
            final Button searchButton = new Button("Search");
            searchButton.setOnAction(e -> search());
            HBox.setHgrow(searchButton, Priority.NEVER);
            searchFieldContainer.getChildren().addAll(searchField, searchButton);
            content.getChildren().add(searchFieldContainer);
            
            searchField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
                if (new KeyCodeCombination(KeyCode.ENTER).match(e)) {
                    search();
                } else if (new KeyCodeCombination(KeyCode.ESCAPE).match(e)) {
                    close();
                }
            });
            
            notFoundText.setFill(Color.RED);
            notFoundText.setVisible(false);
            content.getChildren().add(notFoundText);
            
            setOnShown(e -> {
                Platform.runLater(() -> searchField.requestFocus());
            });
            
            setScene(new Scene(content));
            
        }
        
        private void search() {
            if (searchField.textProperty().isNotEmpty().get()) {
                final String text = getText();
                int caretPosition = getCaretPosition();
                
                if (caretPosition < 0) {
                    caretPosition = 0;
                }
                
                final String searchedText = searchField.getText();
                int position = text.indexOf(searchedText, caretPosition);
                
                if (position < 0 && caretPosition > 0) {
                    position = text.indexOf(searchedText);
                }
                
                if (position < 0) {
                    notFoundText.setVisible(true);
                } else {
                    notFoundText.setVisible(false);
                    
                    positionCaret(position);
                    selectPositionCaret(position + searchedText.length());
                }
                
            }
            
        }
        
    }

}
