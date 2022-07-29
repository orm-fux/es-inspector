package com.github.ormfux.esi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.github.ormfux.esi.model.table.JsonDataRow;
import com.github.ormfux.esi.model.table.JsonDataTable;
import com.github.ormfux.esi.ui.component.JsonNodeTree;
import com.github.ormfux.simple.di.annotations.Bean;
import com.github.ormfux.simple.di.annotations.BeanConstructor;

import java.util.Iterator;
import java.util.Map.Entry;

@Bean
public class QueryResultTransformService {

    private static final String SOURCE_FIELD = "_source";
    private static final String SCORE_FIELD = "_score";
    private static final String ID_FIELD = "_id";

    private final JsonService jsonService;

    @BeanConstructor
    public QueryResultTransformService(final JsonService jsonService)  {
        this.jsonService = jsonService;
    }

    public JsonNodeTree createJsonFXTree(final String json, final int initialExpandedLevels, final int lazyLevel) {
        final JsonNode jsonRootNote = jsonService.readTreeFromPath(json, null);
        return new JsonNodeTree(jsonRootNote, initialExpandedLevels, lazyLevel);
    }

    public JsonDataTable createTable(final String esQueryResult) {
        final JsonNode tree = jsonService.readTreeFromPath(esQueryResult, "/hits/hits");

        if (tree instanceof ArrayNode) {
            final ArrayNode rows = (ArrayNode) tree;
            final JsonDataTable table = new JsonDataTable();
            table.addColumn(ID_FIELD);
            table.addColumn(SCORE_FIELD);

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                final JsonNode row = rows.get(rowIndex);
                addRow(table, row);
            }

            return table;
        } else {
            return null;
        }
    }

    private void addRow(final JsonDataTable table, final JsonNode rowData) {
        final JsonDataRow row = new JsonDataRow(rowData);

        final JsonNode idNode = rowData.get(ID_FIELD);

        if (idNode.isValueNode()) {
            row.addColumnValue(ID_FIELD, idNode.asText());
        }

        final JsonNode scoreNode = rowData.get(SCORE_FIELD);

        if (scoreNode.isValueNode()) {
            row.addColumnValue(SCORE_FIELD, scoreNode.asText());
        }

        final JsonNode rowValues = rowData.get(SOURCE_FIELD);

        if (rowValues != null) {
            addRowValues(table, row, rowValues, null);
        }

        table.getRows().add(row);
    }

    private void addRowValues(final JsonDataTable table, final JsonDataRow row, final JsonNode rowData, final String columnNamePrefix) {
        if (rowData instanceof ArrayNode) {
            final ArrayNode listData = (ArrayNode) rowData;

            for (int valueIndex = 0; valueIndex < listData.size(); valueIndex++) {
                addRowValues(table, row, listData.get(valueIndex), createNextArrayColumnPrefix(columnNamePrefix, valueIndex));
            }

        } else if (rowData instanceof ValueNode) {
            table.addColumn(columnNamePrefix);
            row.addColumnValue(columnNamePrefix, rowData.asText());

        } else {
            final Iterator<Entry<String, JsonNode>> rowDataFields = rowData.fields();

            while (rowDataFields.hasNext()) {
                final Entry<String, JsonNode> rowDataField = rowDataFields.next();
                addRowValues(table, row, rowDataField.getValue(), createNextColumnPrefix(columnNamePrefix, rowDataField.getKey()));
            }
        }
    }

    private String createNextColumnPrefix(final String previousPrefix, final String newSuffix) {
        if (previousPrefix == null) {
            return newSuffix;
        } else {
            return previousPrefix + '.' + newSuffix;
        }
    }

    private String createNextArrayColumnPrefix(final String previousPrefix, final int valueIndex) {
        if (previousPrefix == null) {
            return "[" + valueIndex + "]";
        } else {
            return previousPrefix + '[' + valueIndex + ']';
        }
    }

}
