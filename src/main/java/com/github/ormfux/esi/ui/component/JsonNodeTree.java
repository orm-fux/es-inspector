package com.github.ormfux.esi.ui.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.scene.control.TreeItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringJoiner;

@Data
@EqualsAndHashCode(callSuper = true)
public class JsonNodeTree extends TreeItem<String> {

    private final String name;

    @Getter
    private final JsonNode jsonNode;

    private final int nodeLevel;

    private LazyChildrenStatus lazyStatus;

    public JsonNodeTree(final JsonNode jsonNode, final int initialExpandLevel, final int lazyLevel) {
        this(jsonNode, null, 0, lazyLevel);
        expandLevels(this, initialExpandLevel);
    }

    public boolean loadNextLazyChildrenBatch() {
        if (lazyStatus.isLazyItem()) {
            return addChildrenBatch(nodeLevel, nodeLevel);
        } else {
            return getChildren().stream()
                    .map(child -> (JsonNodeTree) child)
                    .map(JsonNodeTree::loadNextLazyChildrenBatch)
                    .reduce(false, (result, value) -> result || value);
        }
    }

    private JsonNodeTree(final JsonNode jsonNode, final String name, final int currentLevel, final int lazyLevel) {
        this.name = name;
        this.jsonNode = jsonNode;
        this.nodeLevel = currentLevel;
        setValue(getCollapsedLabel());
        defineChildren(currentLevel, lazyLevel);

        expandedProperty().addListener((prop, oldExpanded, expanded) -> {
            if (expanded && !jsonNode.isValueNode()) {
                setValue(name);
            } else {
                setValue(getCollapsedLabel());
            }
        });
    }

    private void expandLevels(final TreeItem<String> item, final int levels) {
        if (levels > 0) {
            item.setExpanded(true);
            item.getChildren().forEach(child -> expandLevels(child, levels - 1));
        }
    }

    private String getCollapsedLabel() {
        final StringJoiner joiner = new StringJoiner(": ");

        if (name != null) {
            joiner.add(name);
        }

        joiner.add(jsonNode.toString());

        return joiner.toString();
    }

    private void defineChildren(final int currentLevel, final int lazyLevel) {
        if (currentLevel + 1 == lazyLevel) {
            this.lazyStatus = LazyChildrenStatus.createLazyStatus();
        } else {
            this.lazyStatus = LazyChildrenStatus.createEagerStatus();
        }

        addChildrenBatch(currentLevel, lazyLevel);
    }

    private boolean addChildrenBatch(final int currentLevel, final int lazyLevel) {
        boolean childrenAdded = false;

        if (jsonNode.isArray()) {
            if (lazyStatus.isLazyItem()) {
                final ArrayNode children = (ArrayNode) jsonNode;
                final int childStartIndex = lazyStatus.getLastLazyChildIndex();
                int childEndIndex = childStartIndex + LazyChildrenStatus.LAZY_CHILLD_BATCH_SIZE;

                if (childEndIndex >= children.size()) {
                    childEndIndex = children.size();
                }

                for (int childIndex = childStartIndex; childIndex < childEndIndex; childIndex++) {
                    getChildren().add(new JsonNodeTree(children.get(childIndex), String.valueOf(childIndex), currentLevel + 1, lazyLevel));
                    childrenAdded = true;
                }

                lazyStatus.setLastLazyChildIndex(childEndIndex);

            } else {
                int arrayIndex = 0;

                for (final JsonNode childJson : jsonNode) {
                    getChildren().add(new JsonNodeTree(childJson, String.valueOf(arrayIndex++), currentLevel + 1, lazyLevel));
                    childrenAdded = true;
                }
            }

        } else if (jsonNode.isObject()) {
            final Iterator<Entry<String, JsonNode>> grandChildIterator = jsonNode.fields();

            while (grandChildIterator.hasNext()) {
                final Entry<String, JsonNode> grandChildNode = grandChildIterator.next();
                getChildren().add(new JsonNodeTree(grandChildNode.getValue(), grandChildNode.getKey(), currentLevel + 1, lazyLevel));
                childrenAdded = true;
            }

        }

        return childrenAdded;
    }

    @Data
    private static class LazyChildrenStatus {
        private static final int LAZY_CHILLD_BATCH_SIZE = 20;

        private final boolean lazyItem;

        private int lastLazyChildIndex = 0;

        static LazyChildrenStatus createLazyStatus() {
            return new LazyChildrenStatus(true);
        }

        static LazyChildrenStatus createEagerStatus() {
            return new LazyChildrenStatus(false);
        }

    }
}