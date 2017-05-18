package com.linkedpipes.plugin.transformer.excel.to.csv;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.util.NumberToTextConverter;

import java.util.Calendar;
import java.util.GregorianCalendar;

class CellConverter {

    private final ExcelToCsvConfiguration configuration;

    private FormulaEvaluator evaluator = null;

    public CellConverter(ExcelToCsvConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setEvaluator(FormulaEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public String getCellValue(Cell cell) throws IllegalArgumentException {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_BOOLEAN:
                return convertBooleanCell(cell.getBooleanCellValue());
            case Cell.CELL_TYPE_FORMULA:
                return convertFormulaCell(cell);
            case Cell.CELL_TYPE_NUMERIC:
                return convertNumericCell(cell);
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_ERROR:
                throw new IllegalArgumentException("Wrong cell type: "
                        + cell.getCellType()
                        + " on row: " + Integer.toString(cell.getRowIndex())
                        + " column: " +
                        Integer.toString(cell.getColumnIndex()));
            default:
                throw new IllegalArgumentException("Unknown cell type: "
                        + cell.getCellType()
                        + " on row: " + Integer.toString(cell.getRowIndex())
                        + " column: " +
                        Integer.toString(cell.getColumnIndex()));
        }
    }

    private String convertBooleanCell(boolean value) {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }

    private String convertFormulaCell(Cell cell) {
        if (!configuration.isEvaluateFormulas()) {
            throw new IllegalArgumentException(
                    "Cells with formulas are not supported, "
                            + "  row: " + Integer.toString(cell.getRowIndex())
                            + " column: " +
                            Integer.toString(cell.getColumnIndex()));
        }
        CellValue value = evaluator.evaluate(cell);
        switch (value.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_BOOLEAN:
                return convertBooleanCell(value.getBooleanValue());
            case Cell.CELL_TYPE_NUMERIC:
                return convertNumericValue(cell, value.getNumberValue());
            case Cell.CELL_TYPE_STRING:
                return value.getStringValue();
            default:
                throw new IllegalArgumentException(
                        "Unsupported value type for formula: "
                                + value.getCellType()
                                + " on row: " +
                                Integer.toString(cell.getRowIndex())
                                + " column: " +
                                Integer.toString(cell.getColumnIndex()));
        }
    }

    private String convertNumericCell(Cell cell) {
        if (configuration.isNumericParse() &&
                DateUtil.isCellDateFormatted(cell)) {
            return convertToDate(cell, cell.getNumericCellValue());
        }
        return NumberToTextConverter.toText(cell.getNumericCellValue());
    }

    private String convertToDate(Cell cell, double value) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
        final StringBuilder dateStr = new StringBuilder(10);
        dateStr.append(cal.get(Calendar.YEAR));
        dateStr.append("-");
        dateStr.append(String.format("%02d", cal.get(Calendar.MONTH) + 1));
        dateStr.append("-");
        dateStr.append(String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));
        return dateStr.toString();
    }

    private String convertNumericValue(Cell cell, double value) {
        if (configuration.isNumericParse() &&
                DateUtil.isCellDateFormatted(cell)) {
            return convertToDate(cell, value);
        }
        return NumberToTextConverter.toText(cell.getNumericCellValue());
    }

}
