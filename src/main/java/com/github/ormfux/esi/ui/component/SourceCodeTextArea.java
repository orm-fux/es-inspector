package com.github.ormfux.esi.ui.component;

import java.util.Set;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

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
            }
        });
    }

}
