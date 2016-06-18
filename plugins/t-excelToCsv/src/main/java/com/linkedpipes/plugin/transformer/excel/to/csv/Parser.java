package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.system.api.files.WritableFilesDataUnit;
import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 *
 * @author Petr Škoda
 */
class Parser {

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private final ExcelToCsvConfiguration configuration;

    public Parser(ExcelToCsvConfiguration configuration) {
        this.configuration = configuration;
    }

    public void processEntry(FilesDataUnit.Entry entry,
            WritableFilesDataUnit outputFiles,
            ExceptionFactory exceptionFactory) throws LpException {
        final Workbook workbook;
        try {
            workbook = WorkbookFactory.create(entry.toFile());
        } catch (IOException | InvalidFormatException ex) {
            throw exceptionFactory.failed("Can't open workbook file.", ex);
        }
        for (int index = 0; index < workbook.getNumberOfSheets(); ++index) {
            final Sheet sheet = workbook.getSheetAt(index);
            try {
                if (configuration.getSheetFilter() != null
                        && !configuration.getSheetFilter().isEmpty()
                        && !sheet.getSheetName().matches(
                                configuration.getSheetFilter())) {
                    // Skip the sheet as it does not match non empty
                    // sheet filter.
                    continue;
                }
            } catch (PatternSyntaxException ex) {
                throw exceptionFactory.failed(
                        "Invalid regular expression for sheet filter.", ex);
            }
            // Create output file name.
            final String outputFileName
                    = configuration.getFileNamePattern().
                    replace(ExcelToCsvConfiguration.FILE_HOLDER,
                            entry.getFileName()).
                    replace(ExcelToCsvConfiguration.SHEET_HOLDER,
                            sheet.getSheetName());
            final File outputFile
                    = outputFiles.createFile(outputFileName).toFile();
            LOG.info("Parsing sheet: '{}' number of rows: {} into file: {}",
                    sheet.getSheetName(), sheet.getLastRowNum(), outputFile);
            try (PrintStream outputStream = new PrintStream(
                    new FileOutputStream(outputFile), false, "UTF-8")) {
                processSheet(sheet, outputStream);
            } catch (IOException ex) {
                throw exceptionFactory.failed("Can't write output to file.",
                        ex);
            }
        }
    }

    private void processSheet(Sheet sheet, PrintStream outputStream)
            throws LpException {
        final int rowEnd;
        if (configuration.getRowsEnd() == -1) {
            rowEnd = sheet.getLastRowNum();
        } else {
            rowEnd = Math.min(configuration.getRowsEnd(), sheet.getLastRowNum());
        }
        // Read virtual columns;
        final List<String> virtualColumns = new ArrayList<>(
                configuration.getVirtualColumns().size());
        final List<String> virtualNames = new ArrayList<>(
                configuration.getVirtualColumns().size());
        for (ExcelToCsvConfiguration.VirtualColumn virtualCell
                : configuration.getVirtualColumns()) {
            virtualColumns.add(getCellValue(sheet, virtualCell));
            virtualNames.add(virtualCell.getName());
        }
        if (configuration.isIncludeSheetName()) {
            virtualColumns.add(sheet.getSheetName());
            virtualNames.add("sheet_name");
        }
        // Determine number of columns.
        final int columnCount;
        final int columnStart = configuration.getColumnsStart();
        if (configuration.getColumnsEnd() == -1) {
            final Row row = sheet.getRow(configuration.getRowsStart());
            columnCount = row.getLastCellNum() - columnStart;
        } else {
            columnCount = configuration.getColumnsEnd() - columnStart + 1;
        }
        // We need to add +1 as we read less then this numner
        // and we want the last column to be also included - as it's
        // in the same way for columns.
        final int columnToRead = columnStart + columnCount;
        // Create row template.
        final List<String> emptyRow = new ArrayList<>(
                columnCount + virtualColumns.size());
        for (int i = 0; i < columnCount; i++) {
            emptyRow.add("");
        }
        // Parse rows and columns.
        boolean firstRow = true;
        for (int rowIndex = configuration.getRowsStart();
                rowIndex <= rowEnd; ++rowIndex) {
            final Row row = sheet.getRow(rowIndex);
            // rowValues can contains null that are sanitized in the
            // sanitizeValue before usege.
            final List<String> rowValues = new ArrayList<>(emptyRow);
            // Check for empty row.
            if (row == null) {
                if (configuration.isSkipEmptyRows()) {
                    continue;
                }
            } else {
                // Determine last column presented in row,
                final int columnEnd
                        = Math.min(columnToRead, row.getLastCellNum());
                // If columnEnd < columnStart, then no columns will be read.
                // That's ok as we have values from the default emptyRow.
                int index = 0;
                for (int columnIndex = columnStart;
                        columnIndex < columnEnd; ++columnIndex) {
                    // Read valus into a list of strings
                    // - as row may not start with first column
                    // we can get some null values here.
                    rowValues.set(index++,
                            getCellValue(row.getCell(columnIndex)));
                }
            }
            // Append virtual columns, if there is header and we
            // are on the first line -> header
            // we append virtual columns names instead of values.
            if (firstRow && configuration.isHeaderPresented()) {
                firstRow = false;
                rowValues.addAll(virtualNames);
            } else {
                rowValues.addAll(virtualColumns);
            }
            // Print output with given columns.
            boolean first = true;
            for (String item : rowValues) {
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
    }

    /**
     * Escape value and convert it to string. Replace null with an empty string.
     *
     * @param value
     * @return
     */
    private String sanitizeValue(String value) {
        if (value == null) {
            return "";
        } else {
            return value.replaceAll("\"", "\"\"");
        }
    }

    /**
     *
     * @param sheet
     * @param virtualColumn
     * @return String value of cell determined by {@link ExcelToCsvConfiguration.VirtualColumn}.
     */
    private String getCellValue(Sheet sheet,
            ExcelToCsvConfiguration.VirtualColumn virtualColumn) {
        final Row row = sheet.getRow(virtualColumn.getRow() - 1);
        if (row == null) {
            // Missing row.
            return "";
        }
        return getCellValue(row.getCell(virtualColumn.getColumn() - 1));
    }

    /**
     *
     * @param cell
     * @return String value of given cell, empty string if cell is null.
     * @throws IllegalArgumentException
     */
    private String getCellValue(Cell cell) throws IllegalArgumentException {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                if (cell.getBooleanCellValue()) {
                    return "true";
                } else {
                    return "false";
                }
            case Cell.CELL_TYPE_ERROR:
            case Cell.CELL_TYPE_FORMULA:
                throw new IllegalArgumentException("Wrong cell type: "
                        + cell.getCellType()
                        + " on row: " + Integer.toString(cell.getRowIndex())
                        + " column: " + Integer.toString(cell.getColumnIndex()));
            case Cell.CELL_TYPE_NUMERIC:
                if (configuration.isNumericParse()) {
                    // DataFormatter.formatCellValue use custom date format.
                    if (DateUtil.isCellDateFormatted(cell)) {
                        final Calendar cal = new GregorianCalendar();
                        cal.setTime(HSSFDateUtil.getJavaDate(
                                cell.getNumericCellValue()));
                        final StringBuilder dateStr = new StringBuilder(10);
                        dateStr.append(cal.get(Calendar.YEAR));
                        dateStr.append("-");
                        dateStr.append(String.format("%02d",
                                cal.get(Calendar.MONTH) + 1));
                        dateStr.append("-");
                        dateStr.append(String.format("%02d",
                                cal.get(Calendar.DAY_OF_MONTH)));
                        return dateStr.toString();
                    }
                }
                // formatRawCellContents
                CsvDataFormatter format = new CsvDataFormatter(true);
                return format.formatCellValue(cell);
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                throw new IllegalArgumentException("Unknown cell type: "
                        + cell.getCellType()
                        + " on row: " + Integer.toString(cell.getRowIndex())
                        + " column: " + Integer.toString(cell.getColumnIndex()));
        }
    }

}
