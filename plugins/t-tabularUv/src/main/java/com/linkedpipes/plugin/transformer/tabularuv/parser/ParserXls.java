package com.linkedpipes.plugin.transformer.tabularuv.parser;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2.ColumnType;
import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2.NamedCell_V1;
import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdf;
import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdfConfigurator;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ParserXls implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(
            ParserXlsConfig.class);

    /**
     * Name of column where sheet name is stored.
     */
    public static final String SHEET_COLUMN_NAME = "__SheetName__";

    private final ParserXlsConfig config;

    private final TableToRdf tableToRdf;

    private int rowNumber = 0;

    public ParserXls(ParserXlsConfig config, TableToRdf tableToRdf) {
        this.config = config;
        this.tableToRdf = tableToRdf;
    }

    @Override
    public void parse(File inFile) throws LpException, ParseFailed {
        final Workbook wb;
        try {
            wb = WorkbookFactory.create(inFile);
        } catch (IOException | InvalidFormatException ex) {
            throw new ParseFailed("WorkbookFactory creation failure.", ex);
        }
        // get sheet to process
        final List<Integer> toProcess = new LinkedList<>();
        for (Integer index = 0; index < wb.getNumberOfSheets(); ++index) {
            if (config.sheetName == null || config.sheetName.isEmpty()
                    ||
                    config.sheetName.compareTo(wb.getSheetName(index)) == 0) {
                // add
                toProcess.add(index);
            }
        }
        // process selected sheets
        for (Integer sheetIndex : toProcess) {
            parseSheet(wb, sheetIndex);
        }
    }

    /**
     * Parse given sheet.
     *
     * @param wb
     * @param sheetIndex
     */
    public void parseSheet(Workbook wb, Integer sheetIndex)
            throws ParseFailed, LpException {

        LOG.debug("parseSheet({}, {})", wb.getSheetName(sheetIndex),
                sheetIndex);

        // for every row
        final Sheet sheet = wb.getSheetAt(sheetIndex);
        if (config.numberOfStartLinesToIgnore > sheet.getLastRowNum()) {
            // no data to parse
            return;
        }
        // generate column names
        int startRow = config.numberOfStartLinesToIgnore;
        List<String> columnNames;
        // Size of original header from file, used to expand/strip content.
        Integer tableHeaderSize = null;
        if (config.hasHeader) {
            // parse line for header
            final Row row = sheet.getRow(startRow++);
            if (row == null) {
                throw new ParseFailed("Header row is null!");
            }
            final int columnStart = row.getFirstCellNum();
            final int columnEnd = row.getLastCellNum();
            columnNames = new ArrayList<>(columnEnd);
            for (int columnIndex = columnStart; columnIndex < columnEnd;
                    columnIndex++) {
                final Cell cell = row.getCell(columnIndex);
                if (cell == null) {
                    // The cell is missing, this happen for example if
                    // document is exported from gdocs. We just log and use
                    // 'null' as cell value.
                    LOG.info("Header cell is null ({}, {}) on '{}'!",
                            startRow - 1, columnIndex,
                            wb.getSheetName(sheetIndex));
                    columnNames.add(null);
                } else {
                    final String name = this.getCellValue(cell);
                    columnNames.add(name);
                }
            }
            if (config.stripHeader) {
                // Remove trailing null values.
                int initialSize = columnNames.size();
                for (int i = columnNames.size() - 1; i > 0; --i) {
                    if (columnNames.get(i) == null) {
                        columnNames.remove(i);
                    } else {
                        // Non null value.
                        break;
                    }
                }
                LOG.info("Removal of nulls changed header size from {} to {}",
                        initialSize, columnNames.size());
            } else {
                LOG.debug("Header size {}", columnNames.size());
            }
            // global names will be added later
            tableHeaderSize = columnNames.size();
        } else {
            columnNames = null;
        }

        //
        // prepare static cells
        //
        final List<String> namedCells = new LinkedList<>();
        for (NamedCell_V1 namedCell : config.namedCells) {
            final Row row = sheet.getRow(namedCell.getRowNumber() - 1);
            if (row == null) {
                throw new ParseFailed("Row for named cell is null! ("
                        + namedCell.getName() + ")");
            }
            final Cell cell = row.getCell(namedCell.getColumnNumber() - 1);
            if (cell == null) {
                throw new ParseFailed("Cell for named cell is null! ("
                        + namedCell.getName() + ")");
            }
            // get value and add to namedCells
            final String value = getCellValue(cell);
            LOG.debug("static cell {} = {}", namedCell.getName(), value);
            namedCells.add(value);
        }
        //
        // parse data row by row
        //
        if (config.rowLimit == null) {
            LOG.debug("Row limit: not used");
        } else {
            LOG.debug("Row limit: {}", config.rowLimit);
        }
        // set if for first time or if we use static row counter
        if (!config.checkStaticRowCounter || rowNumber == 0) {
            rowNumber = config.hasHeader ? 2 : 1;
        }
        // go
        boolean headerGenerated = false;

        final int dataEndAtRow;
        if (config.rowLimit != null) {
            // limit number of lines
            dataEndAtRow = startRow + config.rowLimit;
        } else {
            // We increase by one, as we use less < dataEndAtRow,
            // not <= dataEndAtRow
            dataEndAtRow = sheet.getLastRowNum() + 1;
        }

        int skippedLinesCounter = 0;
        for (Integer rowNumPerFile = startRow; rowNumPerFile < dataEndAtRow;
                ++rowNumber, ++rowNumPerFile) {
            // skip till data
            if (rowNumPerFile < config.numberOfStartLinesToIgnore) {
                continue;
            }
            // get row
            final Row row = sheet.getRow(rowNumPerFile);
            if (row == null) {
                continue;
            }
            // We use zero as the first column must be column 1.
            final int columnStart = row.getFirstCellNum();
            final int columnEnd = row.getLastCellNum();
            // generate header
            if (!headerGenerated) {
                headerGenerated = true;
                // use row data to generate types
                final List<ColumnType> types
                        = new ArrayList<>(columnEnd + namedCells.size());
                // If the first column is empty then getFirstCellNum()
                // return ondec of first column with data. But we want col1
                // to always start at the first column.
                for (int columnIndex = 0; columnIndex < columnEnd;
                        columnIndex++) {
                    final Cell cell = row.getCell(columnIndex);
                    if (cell == null) {
                        types.add(null);
                        continue;
                    }
                    types.add(getCellType(cell));
                }
                // Till now column name can be only set in this method
                // if header is presented.
                if (columnNames == null) {
                    LOG.info("Generating column names from: {} to: {}",
                            columnStart, columnEnd);
                    columnNames = new ArrayList<>(columnEnd);
                    // Generate column names, first column is col1. We start
                    // from 0 as we always want start with left most column.
                    // See comment before types generation for more info.
                    int columnIndex = 0;
                    for (int i = 0; i < columnEnd; i++) {
                        columnNames
                                .add("col" + Integer.toString(++columnIndex));
                    }
                    tableHeaderSize = columnNames.size();
                } else {
                    // expand types row. The header might be wider then the
                    // first data row.
                    fitToSize(types, tableHeaderSize);
                }
                // add user defined names
                for (NamedCell_V1 item : config.namedCells) {
                    columnNames.add(item.getName());
                    types.add(ColumnType.String);
                }
                // add global types and names
                columnNames.add(SHEET_COLUMN_NAME);
                types.add(ColumnType.String);
                // configure
                TableToRdfConfigurator.configure(tableToRdf, columnNames,
                        (List) types, startRow);
            }
            // Prepare row.
            final List<String> parsedRow
                    = new ArrayList<>(columnEnd + namedCells.size());
            // parse columns
            for (int columnIndex = 0; columnIndex < columnEnd; columnIndex++) {
                final Cell cell = row.getCell(columnIndex);
                if (cell == null) {
                    parsedRow.add(null);
                } else {
                    parsedRow.add(getCellValue(cell));
                }
            }
            // Check for row null values - this can happen for excel exported
            // from google docs, where the number oof declared data rows
            // is bigger then it should be together with fitToSize we would
            // generate non-existing columns. In order to prevent this
            // we scan an ignore lines with null values only.
            boolean isEmpty = true;
            for (Object value : parsedRow) {
                if (value != null) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                ++skippedLinesCounter;
                continue;
            }
            // expand row if needed
            fitToSize(parsedRow, tableHeaderSize);

            // add named columns first !!
            parsedRow.addAll(namedCells);
            // add global data
            parsedRow.add(wb.getSheetName(sheetIndex));
            // convert into table
            tableToRdf.paserRow((List) parsedRow, rowNumber);

            if ((rowNumPerFile % 1000) == 0) {
                LOG.debug("Row number {} processed.", rowNumPerFile);
            }
        }
        //
        if (skippedLinesCounter != 0) {
            LOG.info("Some lines ({}) were skipped.", skippedLinesCounter);
        }
    }

    /**
     * Shrink or expand the line as read from the table
     * (no named expectedSize presented).
     *
     * @param row
     * @param expectedSize If null no transformation is done.
     */
    private void fitToSize(List<?> row, Integer expectedSize) {
        if (expectedSize == null) {
            throw new RuntimeException("Row expectedSize is not set!");
        }
        if (row.size() == expectedSize) {
            // This is ok.
        } else if (row.size() > expectedSize) {
            // Shrink and drop data. As they are outside the header,
            // named column would get corrupted by them.
            while (row.size() > expectedSize) {
                row.remove(row.size() - 1);
            }
        } else {
            while (row.size() < expectedSize) {
                row.add(row.size(), null);
            }
        }
    }

    /**
     * Get value of given cell.
     *
     * @param cell
     * @return
     */
    private String getCellValue(Cell cell) throws IllegalArgumentException {
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
                LOG.info("Formula value: {}", cell.getStringCellValue());
                throw new IllegalArgumentException("Wrong cell type: "
                        + cell.getCellType()
                        + " on row: " + Integer.toString(cell.getRowIndex())
                        + " column: " +
                        Integer.toString(cell.getColumnIndex()));
            case Cell.CELL_TYPE_NUMERIC:
                return parseNumericCell(cell);
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                throw new IllegalArgumentException("Unknown cell type: "
                        + cell.getCellType()
                        + " on row: " + Integer.toString(cell.getRowIndex())
                        + " column: " +
                        Integer.toString(cell.getColumnIndex()));
        }
    }

    private String parseNumericCell(Cell cell) {
        if (config.advancedDoubleParser) {
            // Check for Date
            //  https://poi.apache.org/faq.html#faq-N1008D FAQ 8
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
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
            //
            if (config.useDataFormatter) {
                DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            }
            // Can be double or long/integer.
            final double doubleValue = cell.getNumericCellValue();
            // Check if the value is decimal or not.
            if ((doubleValue % 1) == 0) {
                // It's integer or long.
                return Long.toString((long) doubleValue);
            } else {
                return Double.toString(doubleValue);
            }
        } else {
            if (config.useDataFormatter) {
                DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            }
            return Double.toString(cell.getNumericCellValue());
        }
    }

    /**
     * Return type for based on given cell.
     *
     * @param cell
     * @return
     */
    private ColumnType getCellType(Cell cell) throws IllegalArgumentException {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                return ColumnType.Boolean;
            case Cell.CELL_TYPE_ERROR:
                throw new IllegalArgumentException("Cell type is error.");
            case Cell.CELL_TYPE_FORMULA:
                throw new IllegalArgumentException(
                        "The cell contains a formula: " +
                                cell.getCellFormula());
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return ColumnType.Date;
                } else {
                    final String value = (new Double(
                            cell.getNumericCellValue())).toString();
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        return ColumnType.Double;
                    }
                    return ColumnType.Integer;
                }
            case Cell.CELL_TYPE_STRING:
                return ColumnType.String;
            default:
                throw new IllegalArgumentException("Unknown cell type.");
        }
    }

}
