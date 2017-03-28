package com.linkedpipes.plugin.transformer.tabularuv.parser;

import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2.NamedCell_V1;

import java.util.Collections;
import java.util.List;

/**
 * Configuration for {@link ParserXls}.
 */
public class ParserXlsConfig {

    /**
     * If null then every sheet is used.
     */
    final String sheetName;

    final int numberOfStartLinesToIgnore;

    final boolean hasHeader;

    final List<NamedCell_V1> namedCells;

    final Integer rowLimit;

    final boolean checkStaticRowCounter;

    final boolean stripHeader;

    final boolean advancedDoubleParser;

    public ParserXlsConfig(String sheetName, int numberOfStartLinesToIgnore,
            boolean hasHeader, List<NamedCell_V1> namedCells,
            Integer rowLimit, boolean checkStaticRowCounter,
            boolean stripHeader, boolean advancedDoubleParser) {
        this.sheetName = sheetName;
        this.numberOfStartLinesToIgnore = numberOfStartLinesToIgnore;
        this.hasHeader = hasHeader;
        if (namedCells == null) {
            this.namedCells = Collections.EMPTY_LIST;
        } else {
            this.namedCells = namedCells;
        }
        this.rowLimit = rowLimit;
        this.checkStaticRowCounter = checkStaticRowCounter;
        this.stripHeader = stripHeader;
        this.advancedDoubleParser = advancedDoubleParser;
    }

}
