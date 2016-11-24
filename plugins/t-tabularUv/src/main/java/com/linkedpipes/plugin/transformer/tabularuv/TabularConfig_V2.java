package com.linkedpipes.plugin.transformer.tabularuv;

import com.linkedpipes.etl.component.api.service.RdfToPojo;
import com.linkedpipes.plugin.transformer.tabularuv.column.ValueGeneratorReplace;
import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdfConfig;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParserCsvConfig;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParserDbfConfig;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParserType;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParserXlsConfig;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#Configuration")
public class TabularConfig_V2 {

    public enum ColumnType {
        String,
        Integer,
        Long,
        Double,
        Float,
        Date,
        Boolean,
        gYear,
        Decimal,
        /**
         * Auto from data.
         */
        Auto
    }

    @RdfToPojo.Type(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#AdvancedMapping")
    public static class AdvanceMapping {

        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#uri")
        private String uri = "";

        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#template")
        private String template = "";

        public AdvanceMapping() {
        }

        public AdvanceMapping(String uri, String template) {
            this.uri = uri;
            this.template = template;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

    }

    @RdfToPojo.Type(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#ColumnInfo")
    public static class ColumnInfo_V1 {

        /**
         * Column name.
         */
        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#name")
        private String name = null;

        /**
         * Used column URI.
         */
        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#uri")
        private String URI = null;

        /**
         * Final column type.
         */
        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#type")
        private ColumnType type = ColumnType.Auto;

        /**
         * If true then we use information from DBF to determine data type.
         */
        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#typeFromDbf")
        private Boolean useTypeFromDfb = null;

        /**
         * If {@link #type} is {@link ColumnType#String} then this value is used to
         * specify language.
         */
        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#lang")
        private String language = null;

        public ColumnInfo_V1() {
        }

        public ColumnInfo_V1(String URI, ColumnType type) {
            this.URI = URI;
            this.type = type;
        }

        public ColumnInfo_V1(String URI) {
            this.URI = URI;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getURI() {
            return this.URI;
        }

        public void setURI(String URI) {
            this.URI = URI;
        }

        public ColumnType getType() {
            return this.type;
        }

        public void setType(ColumnType type) {
            this.type = type;
        }

        public Boolean isUseTypeFromDfb() {
            return this.useTypeFromDfb;
        }

        public void setUseTypeFromDfb(Boolean useTypeFromDfb) {
            this.useTypeFromDfb = useTypeFromDfb;
        }

        public String getLanguage() {
            return this.language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

    }

    @RdfToPojo.Type(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#NamedCell")
    public static class NamedCell_V1 {

        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#name")
        private String name = "A0";

        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#rowNumber")
        private Integer rowNumber = 0;

        @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#columnNumber")
        private Integer columnNumber = 0;

        public NamedCell_V1() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(Integer rowNumber) {
            this.rowNumber = rowNumber;
        }

        public Integer getColumnNumber() {
            return columnNumber;
        }

        public void setColumnNumber(Integer columnNumber) {
            this.columnNumber = columnNumber;
        }

    }

    /**
     * Name of column that will be used as a key. If null then first column
     * is used. Can also contains template for constriction of primary subject.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#keyColumn")
    private String keyColumn = null;

    /**
     * Base URI that is used to prefix generated URIs.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#baseUri")
    private String baseURI = "http://localhost";

    /**
     * Column mapping simple.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#column")
    private List<ColumnInfo_V1> columnsInfo = new LinkedList<>();

    /**
     * Advanced column mapping using string templates directly. Based on
     * http://w3c.github.io/csvw/csv2rdf/#
     *
     * If { or } should be used then they can be escaped like: \{ and \}
     * this functionality is secured by
     * {@link ValueGeneratorReplace#compile}
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#advancedMapping")
    private List<AdvanceMapping> columnsInfoAdv = new LinkedList<>();

    /**
     * Named cells for XLS.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#namedCell")
    private List<NamedCell_V1> namedCells = new LinkedList<>();

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#quote")
    private String quoteChar = "\"";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#delimeter")
    private String delimiterChar = ",";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#linesToIgnore")
    private Integer linesToIgnore = 0;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#encoding")
    private String encoding = "UTF-8";

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#rowsLimit")
    private Integer rowsLimit = null;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#tableType")
    private ParserType tableType = ParserType.CSV;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#hasHeader")
    private boolean hasHeader = true;

    /**
     * If false only columns from {@link #columnsInfo} are used.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#generateNew")
    private boolean generateNew = true;

    /**
     * If false then for blank cells the {@link TabularOntology#BLANK_CELL}
     * is inserted.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#ignoreBlankCell")
    private boolean ignoreBlankCells = false;

    /**
     * If true then {@link #keyColumn} is interpreted as advanced = template.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#advancedKey")
    private boolean advancedKeyColumn = false;

    /**
     * If null no class is set.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#rowClass")
    private String rowsClass = TabularOntology.ROW_CLASS.toString();

    /**
     * Sheet name.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#sheetName")
    private String xlsSheetName = null;

    /**
     * If checked same row counter is used for all files. Used only for xsls.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#staticRowCounter")
    private boolean staticRowCounter = false;

    /**
     * If true then triple with row number is generated for each line.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#rowTriple")
    private boolean generateRowTriple = true;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#tableSubject")
    private boolean useTableSubject = false;

    /**
     * If checked then type auto is always set to string.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#autoAsString")
    private boolean autoAsStrings = false;

    /**
     * If true then 'a' predicate with class is generated for table and
     * row entity.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#tableClass")
    private boolean generateTableClass = false;

    /**
     * Generate RDF.LABEL for columns from colum name.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#generateLabels")
    private boolean generateLabels = false;

    /**
     * If set then trailing null values in header are ignored.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#stripHeader")
    private boolean stripHeader = false;

    /**
     * If true then string values are trimmed before used.
     *
     * WARNING: This field is in fact used not only for DBF,
     * but in global scope.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#trimString")
    private boolean dbfTrimString = false;

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#xlsAdvancedParser")
    private boolean xlsAdvancedDoubleParser = false;

    /**
     * If true only info log instead of error in case of missing named column.
     */
    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-tabularUv#ignoreMissingColumn")
    private boolean ignoreMissingColumn = false;

    public TabularConfig_V2() {
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(String keyColumn) {
        this.keyColumn = keyColumn;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public List<ColumnInfo_V1> getColumnsInfo() {
        return columnsInfo;
    }

    public void setColumnsInfo(List<ColumnInfo_V1> columnsInfo) {
        this.columnsInfo = columnsInfo;
    }

    public String getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(String quoteChar) {
        this.quoteChar = quoteChar;
    }

    public String getDelimiterChar() {
        return delimiterChar;
    }

    public void setDelimiterChar(String delimiterChar) {
        this.delimiterChar = delimiterChar;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Integer getLinesToIgnore() {
        return linesToIgnore;
    }

    public void setLinesToIgnore(Integer numberOfStartLinesToIgnore) {
        this.linesToIgnore = numberOfStartLinesToIgnore;
    }

    public Integer getRowsLimit() {
        return rowsLimit;
    }

    public void setRowsLimit(Integer rowLimit) {
        this.rowsLimit = rowLimit;
    }

    public ParserType getTableType() {
        return tableType;
    }

    public void setTableType(ParserType tabelType) {
        this.tableType = tabelType;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public boolean isGenerateNew() {
        return generateNew;
    }

    public void setGenerateNew(boolean generateNew) {
        this.generateNew = generateNew;
    }

    public List<AdvanceMapping> getColumnsInfoAdv() {
        return columnsInfoAdv;
    }

    public void setColumnsInfoAdv(List<AdvanceMapping> columnsInfoAdv) {
        this.columnsInfoAdv = columnsInfoAdv;
    }

    public List<NamedCell_V1> getNamedCells() {
        return namedCells;
    }

    public void setNamedCells(List<NamedCell_V1> namedCells) {
        this.namedCells = namedCells;
    }

    public String getRowsClass() {
        return rowsClass;
    }

    public void setRowsClass(String columnClass) {
        this.rowsClass = columnClass;
    }

    public String getXlsSheetName() {
        return xlsSheetName;
    }

    public void setXlsSheetName(String xlsSheetName) {
        this.xlsSheetName = xlsSheetName;
    }

    public boolean isIgnoreBlankCells() {
        return ignoreBlankCells;
    }

    public void setIgnoreBlankCells(boolean ignoreBlankCells) {
        this.ignoreBlankCells = ignoreBlankCells;
    }

    public boolean isAdvancedKeyColumn() {
        return advancedKeyColumn;
    }

    public void setAdvancedKeyColumn(boolean advancedKeyColumn) {
        this.advancedKeyColumn = advancedKeyColumn;
    }

    public boolean isStaticRowCounter() {
        return staticRowCounter;
    }

    public void setStaticRowCounter(boolean staticRowCounter) {
        this.staticRowCounter = staticRowCounter;
    }

    public boolean isGenerateRowTriple() {
        return generateRowTriple;
    }

    public void setGenerateRowTriple(boolean generateRowTriple) {
        this.generateRowTriple = generateRowTriple;
    }

    public boolean isUseTableSubject() {
        return useTableSubject;
    }

    public void setUseTableSubject(boolean useTableSubject) {
        this.useTableSubject = useTableSubject;
    }

    public Boolean isAutoAsStrings() {
        return autoAsStrings;
    }

    public void setAutoAsStrings(Boolean autoAsStrings) {
        this.autoAsStrings = autoAsStrings;
    }

    public boolean isGenerateTableClass() {
        return generateTableClass;
    }

    public void setGenerateTableClass(boolean tableRowClass) {
        this.generateTableClass = tableRowClass;
    }

    public boolean isGenerateLabels() {
        return generateLabels;
    }

    public void setGenerateLabels(boolean generateLabels) {
        this.generateLabels = generateLabels;
    }

    public boolean isStripHeader() {
        return stripHeader;
    }

    public void setStripHeader(boolean stripHeader) {
        this.stripHeader = stripHeader;
    }

    public boolean isDbfTrimString() {
        return dbfTrimString;
    }

    public void setDbfTrimString(boolean dbfTrimString) {
        this.dbfTrimString = dbfTrimString;
    }

    public boolean isXlsAdvancedDoubleParser() {
        return xlsAdvancedDoubleParser;
    }

    public void setXlsAdvancedDoubleParser(boolean xlsAdvancedDoubleParser) {
        this.xlsAdvancedDoubleParser = xlsAdvancedDoubleParser;
    }

    public boolean isIgnoreMissingColumn() {
        return ignoreMissingColumn;
    }

    public void setIgnoreMissingColumn(boolean ignoreMissingColumn) {
        this.ignoreMissingColumn = ignoreMissingColumn;
    }

    public TableToRdfConfig getTableToRdfConfig() {
        return new TableToRdfConfig(keyColumn, baseURI, columnsInfo,
                generateNew, rowsClass, ignoreBlankCells, columnsInfoAdv,
                advancedKeyColumn, generateRowTriple, autoAsStrings,
                generateTableClass, generateLabels, dbfTrimString,
                ignoreMissingColumn);
    }

    public ParserCsvConfig getParserCsvConfig() {
        return new ParserCsvConfig(quoteChar, delimiterChar,
                encoding, linesToIgnore,
                rowsLimit == null || rowsLimit == -1 ? null : rowsLimit,
                hasHeader, staticRowCounter);
    }

    public ParserDbfConfig getParserDbfConfig() {
        return new ParserDbfConfig(encoding,
                rowsLimit == null || rowsLimit == -1 ? null : rowsLimit,
                staticRowCounter);
    }

    public ParserXlsConfig getParserXlsConfig() {
        return new ParserXlsConfig(xlsSheetName, linesToIgnore, hasHeader,
                namedCells,
                rowsLimit == null || rowsLimit == -1 ? null : rowsLimit,
                staticRowCounter, stripHeader, xlsAdvancedDoubleParser);
    }

}
