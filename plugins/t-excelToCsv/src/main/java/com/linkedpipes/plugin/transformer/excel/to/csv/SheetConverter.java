package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

class SheetConverter {

    private final ExcelToCsvConfiguration configuration;

    private final CellConverter cellConverter;

    private Sheet sheet;

    private PrintStream outputStream;

    private List<String> virtualColumns;

    private List<String> virtualNames;

    /**
     * Can contains null values.
     */
    private List<String> emptyRow;

    public SheetConverter(ExcelToCsvConfiguration configuration) {
        this.configuration = configuration;
        this.cellConverter = new CellConverter(configuration);
    }

    public void setEvaluator(FormulaEvaluator evaluator) {
        cellConverter.setEvaluator(evaluator);
    }

    public void convert(Sheet sheet, PrintStream output) throws
            LpException {
        this.sheet = sheet;
        this.outputStream = output;
        //
        initializeVirtualColumns();
        int rowsEnd = getRowsEnd();
        int columnsStart = configuration.getColumnsStart();
        int columnsCount = getColumnsToReadCount(columnsStart);
        prepareRowTemplate(columnsCount);
        parseRegion(rowsEnd, columnsStart, columnsCount);
    }

    private void initializeVirtualColumns() {
        int virtualColumnsSize = configuration.getVirtualColumns().size();
        virtualColumns = new ArrayList<>(virtualColumnsSize);
        virtualNames = new ArrayList<>(virtualColumnsSize);
        addStaticReferences();
        if (configuration.isIncludeSheetName()) {
            virtualColumns.add(sheet.getSheetName());
            virtualNames.add("sheet_name");
        }
    }

    private void addStaticReferences() {
        for (ExcelToCsvConfiguration.VirtualColumn cell
                : configuration.getVirtualColumns()) {
            virtualColumns.add(getVirtualColumnValue(sheet, cell));
            virtualNames.add(cell.getName());
        }
    }

    private String getVirtualColumnValue(Sheet sheet,
                                         ExcelToCsvConfiguration.VirtualColumn virtualColumn) {
        Row row = sheet.getRow(virtualColumn.getRow() - 1);
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(virtualColumn.getColumn() - 1);
        return cellConverter.getCellValue(cell);
    }

    private int getRowsEnd() {
        if (configuration.getRowsEnd() == -1) {
            return sheet.getLastRowNum();
        } else {
            return Math.min(configuration.getRowsEnd(),
                    sheet.getLastRowNum());
        }
    }

    private int getColumnsToReadCount(int columnsStart) {
        if (configuration.getColumnsEnd() == -1) {
            Row row = getFirstRow();
            return row.getLastCellNum() - columnsStart;
        } else {
            // +1 to also read the last indexed column
            return configuration.getColumnsEnd() - columnsStart + 1;
        }
    }

    private Row getFirstRow() {
        // In some cases the first row may not start with the index 0 but 1.
        // So we use getFirstRowNum to shift the index.
        int startRowIndex = configuration.getRowsStart();
        startRowIndex += sheet.getFirstRowNum();
        return sheet.getRow(startRowIndex);
    }

    private void prepareRowTemplate(int columnsCount) {
        emptyRow = new ArrayList<>(columnsCount + virtualColumns.size());
        for (int i = 0; i < columnsCount; i++) {
            emptyRow.add("");
        }
    }

    private void parseRegion(int rowsEnd, int columnsStart, int columnsCount) {
        boolean firstRow = true;
        for (int rowIndex = configuration.getRowsStart(); rowIndex <= rowsEnd;
             ++rowIndex) {
            Row row = sheet.getRow(rowIndex);
            if (row == null && configuration.isSkipEmptyRows()) {
                continue;
            }
            List<String> values = rowToValues(row, columnsStart, columnsCount);
            if (containsEmptyValues(values)) {
                continue;
            }

            if (firstRow && configuration.isHeaderPresented()) {
                firstRow = false;
                addVirtualHeader(values);
            } else {
                addVirtualColumns(values);
            }
            writeRow(values);
        }
    }

    private boolean containsEmptyValues(List<String> values) {
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private List<String> rowToValues(Row row, int start, int count) {
        if (row == null) {
            return createEmptyRowValues();
        }
        int toCopy = Math.min(start + count, row.getLastCellNum());
        if (start >= toCopy) {
            return createEmptyRowValues();
        }
        List<String> rowValues = createEmptyRowValues();
        int index = 0;
        for (int columnIndex = start; columnIndex < toCopy; ++columnIndex) {
            Cell cell = row.getCell(columnIndex);
            rowValues.set(index++, cellConverter.getCellValue(cell));
        }
        return rowValues;
    }

    private List<String> createEmptyRowValues() {
        return new ArrayList<>(emptyRow);
    }

    private void addVirtualHeader(List<String> values) {
        values.addAll(virtualNames);
    }

    private void addVirtualColumns(List<String> values) {
        values.addAll(virtualColumns);
    }

    private void writeRow(List<String> values) {
        boolean first = true;
        for (String item : values) {
            if (!first) {
                outputStream.print(",");
            }
            first = false;
            outputStream.print("\"");
            outputStream.print(sanitizeValue(item));
            outputStream.print("\"");
        }
        outputStream.print("\n");
    }

    private String sanitizeValue(String value) {
        if (value == null) {
            return "";
        } else {
            return value.replaceAll("\"", "\"\"");
        }
    }

}
