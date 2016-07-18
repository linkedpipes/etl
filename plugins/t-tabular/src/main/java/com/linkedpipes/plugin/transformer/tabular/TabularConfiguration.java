package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.component.api.service.RdfToPojo;
import java.util.LinkedList;
import java.util.List;

/**
 * https://www.w3.org/TR/2015/REC-tabular-metadata-20151217/
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = TabularVocabulary.TABLE)
public class TabularConfiguration {

    @RdfToPojo.Type(uri = TabularVocabulary.SCHEMA)
    public static class Schema {

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_COLUMN)
        private List<Column> columns = new LinkedList<>();

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_PRIMARY_KEY)
        private String primaryKey; // Reference to column.

        /**
         * Inherited property.
         * URI Template.
         *
         * Can be used to define subject of the output.
         */
        @RdfToPojo.Property(uri = TabularVocabulary.HAS_ABOUT_URL)
        private String aboutUrl;

        public Schema() {

        }

        public List<Column> getColumns() {
            return columns;
        }

        public void setColumns(List<Column> columns) {
            this.columns = columns;
        }

        public String getPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(String primaryKey) {
            this.primaryKey = primaryKey;
        }

        public String getAboutUrl() {
            return aboutUrl;
        }

        public void setAboutUrl(String aboutUrl) {
            this.aboutUrl = aboutUrl;
        }

    }

    /**
     * If name and titles are missing name for the column is determined as
     * "_col.[N]", where N is the row number.
     */
    @RdfToPojo.Type(uri = TabularVocabulary.COLUMN)
    public static class Column {

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_NAME)
        private String name;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_REQUIRED)
        private boolean required = true;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_SUPRESS_OUTPUT)
        private boolean supressOutput = false;

        /**
         * Inherited property.
         */
        @RdfToPojo.Property(uri = TabularVocabulary.HAS_LANG)
        private String lang;

        /**
         * Inherited property.
         */
        private String separator;

        /**
         * Inherited property.
         */
        private boolean ordered = false;

        /**
         * Renamed from default as it's the Java key word.
         */
        private String default_;

        /**
         * Inherited property.
         *
         * ! Can be string or complex type. ! For now we suppose it's just
         * string.
         */
        @RdfToPojo.Property(uri = TabularVocabulary.HAS_SUPRESS_DATATYPE)
        private String datatype;

        /**
         * Inherited property -> if missing it's inherit from parent.
         * URI Template.
         *
         * Can be used to define subject of the output.
         */
        @RdfToPojo.Property(uri = TabularVocabulary.HAS_ABOUT_URL)
        private String aboutUrl;

        /**
         * Inherited property.
         * URI Template.
         */
        @RdfToPojo.Property(uri = TabularVocabulary.HAS_PROPERTY_URL)
        private String propertyUrl;

        /**
         * Inherited property.
         * URI Template.
         *
         * Can be used to define value of this column as a URL.
         */
        @RdfToPojo.Property(uri = TabularVocabulary.HAS_VALUE_URL)
        private String valueUrl;

        public Column() {
            // Default data type.
            datatype = "string";
        }

        public Column(String name, String lang, String separator,
                String default_, String datatype, String aboutUrl,
                String propertyUrl, String valueUrl) {
            this.name = name;
            this.lang = lang;
            this.separator = separator;
            this.default_ = default_;
            this.datatype = datatype;
            this.aboutUrl = aboutUrl;
            this.propertyUrl = propertyUrl;
            this.valueUrl = valueUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isSupressOutput() {
            return supressOutput;
        }

        public void setSupressOutput(boolean supressOutput) {
            this.supressOutput = supressOutput;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public boolean isOrdered() {
            return ordered;
        }

        public void setOrdered(boolean ordered) {
            this.ordered = ordered;
        }

        public String getDefault_() {
            return default_;
        }

        public void setDefault_(String default_) {
            this.default_ = default_;
        }

        public String getDatatype() {
            return datatype;
        }

        public void setDatatype(String datatype) {
            this.datatype = datatype;
        }

        public String getAboutUrl() {
            return aboutUrl;
        }

        public void setAboutUrl(String aboutUrl) {
            this.aboutUrl = aboutUrl;
        }

        public String getPropertyUrl() {
            return propertyUrl;
        }

        public void setPropertyUrl(String propertyUrl) {
            this.propertyUrl = propertyUrl;
        }

        public String getValueUrl() {
            return valueUrl;
        }

        public void setValueUrl(String valueUrl) {
            this.valueUrl = valueUrl;
        }

    }

    @RdfToPojo.Type(uri = TabularVocabulary.DIALECT)
    public static class Dialect {

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_COMMENT_PREFIX)
        private String commentPrefix = "#";

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_DOUBLE_QUOTE)
        private String doubleQuote;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_DELIMETER)
        private String delimeter = ",";

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_ENCODING)
        private String encoding = "UTF-8";

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_HEADER)
        private boolean header = false;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_HEADER_ROW_COUNT)
        private String headerRowCount;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_QUOTE_CHAR)
        private String quoteChar = "\"";

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_SKIP_BLANK_ROWS)
        private boolean skipBlankRows = false;

        /**
         * Non zero value will cause number and source number to differ.
         */
        @RdfToPojo.Property(uri = TabularVocabulary.HAS_SKIP_COLUMNS)
        private Integer skipColumns = 0;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_SKIP_INITIAL_SPACE)
        private boolean skipInitialSpace = false;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_SKIP_ROWS)
        private int skipRows = 0;

        @RdfToPojo.Property(uri = TabularVocabulary.HAS_TRIM)
        private boolean trim = true;

        public Dialect() {
        }

        public String getCommentPrefix() {
            return commentPrefix;
        }

        public void setCommentPrefix(String commentPrefix) {
            this.commentPrefix = commentPrefix;
        }

        public String getDoubleQuote() {
            return doubleQuote;
        }

        public void setDoubleQuote(String doubleQuote) {
            this.doubleQuote = doubleQuote;
        }

        public String getDelimeter() {
            return delimeter;
        }

        public void setDelimeter(String delimeter) {
            this.delimeter = delimeter;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public boolean isHeader() {
            return header;
        }

        public void setHeader(boolean header) {
            this.header = header;
        }

        public String getHeaderRowCount() {
            return headerRowCount;
        }

        public void setHeaderRowCount(String headerRowCount) {
            this.headerRowCount = headerRowCount;
        }

        public String getQuoteChar() {
            return quoteChar;
        }

        public void setQuoteChar(String quoteChar) {
            this.quoteChar = quoteChar;
        }

        public boolean isSkipBlankRows() {
            return skipBlankRows;
        }

        public void setSkipBlankRows(boolean skipBlankRows) {
            this.skipBlankRows = skipBlankRows;
        }

        public Integer isSkipColumns() {
            return skipColumns;
        }

        public void setSkipColumns(Integer skipColumns) {
            this.skipColumns = skipColumns;
        }

        public boolean isSkipInitialSpace() {
            return skipInitialSpace;
        }

        public void setSkipInitialSpace(boolean skipInitialSpace) {
            this.skipInitialSpace = skipInitialSpace;
        }

        public int getSkipRows() {
            return skipRows;
        }

        public void setSkipRows(int skipRows) {
            this.skipRows = skipRows;
        }

        public boolean isTrim() {
            return trim;
        }

        public void setTrim(boolean trim) {
            this.trim = trim;
        }

    }

    /**
     * Can be inherit from {@link TableGroup}.
     */
    @RdfToPojo.Property(uri = TabularVocabulary.HAS_TABLE_SCHEMA)
    private Schema tableSchema = new Schema();

    /**
     * Can be inherit from {@link TableGroup}.
     */
    @RdfToPojo.Property(uri = TabularVocabulary.HAS_DIALECT)
    private Dialect dialect = new Dialect();

    @RdfToPojo.Property(uri = TabularVocabulary.HAS_NORMAL_MODE)
    private boolean standardMode = false;

    /**
     * If set, user given columns are ignored.
     */
    @RdfToPojo.Property(uri = TabularVocabulary.HAS_FULL_MAPPING)
    private boolean fullMapping = true;

    @RdfToPojo.Property(uri = TabularVocabulary.HAS_ROW_LIMIT)
    private int rowLimit = -1;

    @RdfToPojo.Property(uri = TabularVocabulary.HAS_USE_BASE_URI)
    private boolean useBaseUri = false;

    @RdfToPojo.Property(uri = TabularVocabulary.HAS_BASE_URI)
    private String baseUri;

    @RdfToPojo.Property(uri = TabularVocabulary.HAS_GENERETE_NULL_HEADER)
    private boolean generateNullHeaderName = false;

    public TabularConfiguration() {
    }

    public Schema getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(Schema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    public boolean isStandardMode() {
        return standardMode;
    }

    public void setStandardMode(boolean standardMode) {
        this.standardMode = standardMode;
    }

    public boolean isFullMapping() {
        return fullMapping;
    }

    public void setFullMapping(boolean fullMapping) {
        this.fullMapping = fullMapping;
    }

    public int getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
    }

    public boolean isUseBaseUri() {
        return useBaseUri;
    }

    public void setUseBaseUri(boolean useBaseUri) {
        this.useBaseUri = useBaseUri;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public boolean isGenerateNullHeaderName() {
        return generateNullHeaderName;
    }

    public void setGenerateNullHeaderName(boolean generateNullHeaderName) {
        this.generateNullHeaderName = generateNullHeaderName;
    }

}
