package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = ExcelToCsvVocabulary.CONFIG)
public class ExcelToCsvConfiguration {

    public static final String FILE_HOLDER = "{FILE}";

    public static final String SHEET_HOLDER = "{SHEET}";

    /**
     * Row and column starts from 1 (as visible in excel).
     */
    @RdfToPojo.Type(iri = ExcelToCsvVocabulary.VIRTUAL_COLUMN)
    public static class VirtualColumn {

        @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_ROW)
        private int row;

        @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_COLUMN)
        private int column;

        @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_NAME)
        private String name;

        public VirtualColumn() {
        }

        public VirtualColumn(int row, int column, String name) {
            this.row = row;
            this.column = column;
            this.name = name;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    /**
     * Pattern used to generate output file name.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_FILE_NAME)
    private String fileNamePattern = SHEET_HOLDER + "-" + FILE_HOLDER + ".csv";

    /**
     * Regexp used to match sheet name.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_SHEET_FILTER)
    private String sheetFilter = ".*";

    /**
     * Number of rows to skip.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_ROW_START)
    private int rowsStart = 0;

    /**
     * Number of initial columns to skip.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_COLUMN_START)
    private int columnsStart = 0;

    /**
     * Number of rows to read at max.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_ROW_END)
    private int rowsEnd = -1;

    /**
     * Should be set, it -1 then the number of rows is determined by the
     * header size.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_COLUMN_END)
    private int columnsEnd = -1;

    /**
     * List of virtual columns, column has value of given cell.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_VIRTUAL_COLUMN)
    private List<VirtualColumn> virtualColumns = new LinkedList<>();

    /**
     * True if data has header.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_HEADER)
    private boolean headerPresented = true;

    /**
     * If true then double values are checked to be dates.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_NUMERIC_PARSE)
    private boolean numericParse = true;

    /**
     * If true empty rows are not put to output.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_SKIP_EMPTY_ROWS)
    private boolean skipEmptyRows = true;

    /**
     * If true sheet name is included as special column named 'sheet_name'.
     */
    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_INCLUDE_SHEET_NAME)
    private boolean includeSheetName = false;

    @RdfToPojo.Property(iri = ExcelToCsvVocabulary.HAS_EVAL_FORMULA)
    private boolean evaluateFormulas = false;

    public ExcelToCsvConfiguration() {
    }

    public String getFileNamePattern() {
        return fileNamePattern;
    }

    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }

    public String getSheetFilter() {
        return sheetFilter;
    }

    public void setSheetFilter(String sheetFilter) {
        this.sheetFilter = sheetFilter;
    }

    public int getRowsStart() {
        return rowsStart;
    }

    public void setRowsStart(int rowsStart) {
        this.rowsStart = rowsStart;
    }

    public int getColumnsStart() {
        return columnsStart;
    }

    public void setColumnsStart(int columnsStart) {
        this.columnsStart = columnsStart;
    }

    public int getRowsEnd() {
        return rowsEnd;
    }

    public void setRowsEnd(int rowsEnd) {
        this.rowsEnd = rowsEnd;
    }

    public int getColumnsEnd() {
        return columnsEnd;
    }

    public void setColumnsEnd(int columnsEnd) {
        this.columnsEnd = columnsEnd;
    }

    public List<VirtualColumn> getVirtualColumns() {
        return virtualColumns;
    }

    public void setVirtualColumns(List<VirtualColumn> virtualColumns) {
        this.virtualColumns = virtualColumns;
    }

    public boolean isHeaderPresented() {
        return headerPresented;
    }

    public void setHeaderPresented(boolean headerPresented) {
        this.headerPresented = headerPresented;
    }

    public boolean isNumericParse() {
        return numericParse;
    }

    public void setNumericParse(boolean numericParse) {
        this.numericParse = numericParse;
    }

    public boolean isSkipEmptyRows() {
        return skipEmptyRows;
    }

    public void setSkipEmptyRows(boolean skipEmptyRows) {
        this.skipEmptyRows = skipEmptyRows;
    }

    public boolean isIncludeSheetName() {
        return includeSheetName;
    }

    public void setIncludeSheetName(boolean includeSheetName) {
        this.includeSheetName = includeSheetName;
    }

    public boolean isEvaluateFormulas() {
        return evaluateFormulas;
    }

    public void setEvaluateFormulas(boolean evaluateFormulas) {
        this.evaluateFormulas = evaluateFormulas;
    }
}
