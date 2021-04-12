package com.github.ormfux.esi.ui.component;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.JsonNode;

import javafx.scene.control.TreeItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode(callSuper = true)
public class JsonNodeTree extends TreeItem<String> {
    
    private final String name;
    
    private final JsonNode jsonNode;
    
    public JsonNodeTree(final JsonNode jsonNode, final int initialExpandLevel) {
        this(jsonNode, null);
        expandLevels(this, initialExpandLevel);
    }
    
    private void expandLevels(final TreeItem<String> item, final int levels) {
        if (levels > 0) {
            item.setExpanded(true);
            item.getChildren().forEach(child -> expandLevels(child, levels - 1));
        }
    }
    
    private JsonNodeTree(final JsonNode jsonNode, final String name) {
        this.name = name;
        this.jsonNode = jsonNode;
        setValue(getCollapsedLabel());
        defineChildren(this);
        
        expandedProperty().addListener((prop, oldExpanded, expanded) -> {
            if (expanded && !jsonNode.isValueNode()) {
                setValue(name);
            } else {
                setValue(getCollapsedLabel());
            }
        });
    }
    
    private String getCollapsedLabel() {
        final StringJoiner joiner = new StringJoiner(": ");
        
        if (name != null) {
            joiner.add(name);
        }
        
        joiner.add(jsonNode.toString());
        
        return joiner.toString();
    }

    private void defineChildren(final JsonNodeTree node) {
        final JsonNode childJsons = node.getJsonNode();
        
        if (childJsons.isArray()) {
            int arrayIndex = 0;
            
            for (final JsonNode childJson : childJsons) {
                node.getChildren().add(new JsonNodeTree(childJson, String.valueOf(arrayIndex++)));
            }
            
        } else if (childJsons.isObject()) {
            final Iterator<Entry<String, JsonNode>> grandChildIterator = childJsons.fields();
            
            while (grandChildIterator.hasNext()) {
                final Entry<String, JsonNode> grandChildNode = grandChildIterator.next();
                node.getChildren().add(new JsonNodeTree(grandChildNode.getValue(), grandChildNode.getKey()));
            }
        }
    }
}