package com.linkedpipes.plugin.transformer.excel.to.csv;

/**
 *
 */
final class ExcelToCsvVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-excelToCsv#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    public static final String HAS_SHEET_FILTER = PREFIX + "sheetFilter";

    public static final String HAS_ROW_START = PREFIX + "rowStart";

    public static final String HAS_COLUMN_START = PREFIX + "columnStart";

    public static final String HAS_ROW_END = PREFIX + "rowEnd";

    public static final String HAS_COLUMN_END = PREFIX + "columnEnd";

    public static final String HAS_VIRTUAL_COLUMN = PREFIX + "virtualColumn";

    public static final String HAS_HEADER = PREFIX + "header";

    public static final String HAS_NUMERIC_PARSE = PREFIX + "numericParse";

    public static final String HAS_SKIP_EMPTY_ROWS = PREFIX + "skipEmptyRows";

    public static final String HAS_INCLUDE_SHEET_NAME = PREFIX + "includeSheetName";

    public static final String VIRTUAL_COLUMN = PREFIX + "VirtualColumn";

    public static final String HAS_ROW = PREFIX + "row";

    public static final String HAS_COLUMN = PREFIX + "column";

    public static final String HAS_NAME = PREFIX + "name";

    public static final String HAS_EVAL_FORMULA = PREFIX + "evalFormula";

    private ExcelToCsvVocabulary() {
    }

}
