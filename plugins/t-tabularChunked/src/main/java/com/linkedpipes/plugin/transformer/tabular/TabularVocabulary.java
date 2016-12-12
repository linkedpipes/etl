package com.linkedpipes.plugin.transformer.tabular;

/**
 *
 */
public final class TabularVocabulary {

    private static final String PREFIX = "http://www.w3.org/ns/csvw#";

    public static final String LOCAL_PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-tabular#";

    public static final String TABLE = PREFIX + "Table";

    public static final String HAS_TABLE_SCHEMA = PREFIX + "tableSchema";

    public static final String HAS_DIALECT = PREFIX + "dialect";

    public static final String HAS_SUPRESS_OUTPUT = PREFIX + "supressOutput";

    public static final String DIALECT = PREFIX + "Dialect";

    public static final String HAS_COMMENT_PREFIX = PREFIX + "commentPrefix";

    public static final String HAS_DOUBLE_QUOTE = PREFIX + "doubleQuote";

    public static final String HAS_DELIMETER = PREFIX + "delimeter";

    public static final String HAS_ENCODING = PREFIX + "encoding";

    public static final String HAS_HEADER = PREFIX + "header";

    public static final String HAS_HEADER_ROW_COUNT = PREFIX + "headerRowCount";

    public static final String HAS_QUOTE_CHAR = PREFIX + "quoteChar";

    public static final String HAS_SKIP_BLANK_ROWS = PREFIX + "skipBlankRows";

    public static final String HAS_SKIP_COLUMNS = PREFIX + "skipColumns";

    public static final String HAS_SKIP_INITIAL_SPACE = PREFIX
            + "skipInitialSpace";

    public static final String HAS_CHUNK_SIZE = LOCAL_PREFIX + "chunkSize";

    public static final String HAS_SKIP_ROWS = PREFIX + "skipRows";

    public static final String HAS_TRIM = PREFIX + "trim";

    public static final String COLUMN = PREFIX + "Column";

    public static final String HAS_NAME = PREFIX + "name";

    public static final String HAS_REQUIRED = PREFIX + "required";

    public static final String HAS_LANG = PREFIX + "lang";

    public static final String HAS_SUPRESS_DATATYPE = PREFIX + "datatype";

    public static final String SCHEMA = PREFIX + "Schema";

    public static final String HAS_COLUMN = PREFIX + "column";

    public static final String HAS_PRIMARY_KEY = PREFIX + "primaryKey";

    public static final String HAS_ABOUT_URL = PREFIX + "aboutUrl";

    public static final String HAS_PROPERTY_URL = PREFIX + "propertyUrl";

    public static final String HAS_VALUE_URL = PREFIX + "valueUrl";

    public static final String HAS_NORMAL_MODE = LOCAL_PREFIX + "normalOutput";

    public static final String HAS_FULL_MAPPING = LOCAL_PREFIX + "fullMapping";

    public static final String HAS_ROW_LIMIT = LOCAL_PREFIX + "rowLimit";

    public static final String HAS_USE_BASE_URI = LOCAL_PREFIX + "useBaseUri";

    public static final String HAS_BASE_URI = LOCAL_PREFIX + "baseUri";

    public static final String HAS_GENERETE_NULL_HEADER = LOCAL_PREFIX
            + "generateNullHeaderNames";

    public static final String HAS_ENCODE_TYPE = LOCAL_PREFIX +
            "encodeType";

    private TabularVocabulary() {
    }

}
