package com.linkedpipes.plugin.transformer.tabularuv.mapper;

import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2;
import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2.ColumnInfo_V1;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * See main configuration for more details about fields meaning.
 */
public class TableToRdfConfig {

    /**
     * Name of column with key, null, or template.
     */
    final String keyColumn;

    /**
     * Base URI used to prefix generated URIs.
     */
    final String baseURI;

    /**
     * User configuration about parsing process.
     */
    final Map<String, ColumnInfo_V1> columnsInfo;

    /**
     * Advanced configuration about parsing.
     */
    final List<TabularConfig_V2.AdvanceMapping> columnsInfoAdv;

    /**
     * If true then new column, not specified in {@link #columnsInfo},
     * can be added.
     */
    final boolean generateNew;

    /**
     * Metadata for column - type.
     */
    final String rowsClass;

    final boolean ignoreBlankCells;

    final boolean advancedKeyColumn;

    final boolean generateRowTriple;

    final boolean autoAsStrings;

    final boolean generateTableClass;

    final boolean generateLabels;

    final boolean trimString;

    final boolean ignoreMissingColumn;

    public TableToRdfConfig(String keyColumnName, String baseURI,
            List<ColumnInfo_V1> columnsInfo, boolean generateNew,
            String rowsClass, boolean ignoreBlankCells,
            List<TabularConfig_V2.AdvanceMapping> columnsInfoAdv,
            boolean advancedKeyColumn, boolean generateRowTriple,
            boolean autoAsStrings, boolean generateTableRowClass,
            boolean generateLabels, boolean trimString,
            boolean ignoreMissingColumn) {
        this.keyColumn = keyColumnName;
        this.baseURI = baseURI;
        //
        this.columnsInfo = new HashMap<>();
        for (ColumnInfo_V1 item : columnsInfo) {
            this.columnsInfo.put(item.getName(), item);
        }
        //
        this.generateNew = generateNew;
        this.rowsClass = rowsClass;
        this.ignoreBlankCells = ignoreBlankCells;
        this.columnsInfoAdv = columnsInfoAdv != null
                ? columnsInfoAdv : Collections.EMPTY_LIST;
        this.advancedKeyColumn = advancedKeyColumn;
        this.generateRowTriple = generateRowTriple;
        this.autoAsStrings = autoAsStrings;
        this.generateTableClass = generateTableRowClass;
        this.generateLabels = generateLabels;
        this.trimString = trimString;
        this.ignoreMissingColumn = ignoreMissingColumn;
    }

}
