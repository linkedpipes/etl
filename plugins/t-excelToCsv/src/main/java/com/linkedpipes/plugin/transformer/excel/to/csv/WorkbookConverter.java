package com.linkedpipes.plugin.transformer.excel.to.csv;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

class WorkbookConverter {

    private static final Logger LOG =
            LoggerFactory.getLogger(WorkbookConverter.class);

    private final ExcelToCsvConfiguration configuration;

    private final WritableFilesDataUnit outputFiles;

    private final SheetConverter sheetConverter;

    public WorkbookConverter(
            ExcelToCsvConfiguration configuration,
            WritableFilesDataUnit outputFiles) {
        this.configuration = configuration;
        this.outputFiles = outputFiles;
        this.sheetConverter = new SheetConverter(configuration);
    }

    public void processEntry(FilesDataUnit.Entry entry) throws LpException {
        Workbook workbook = loadWorkbook(entry);
        if (configuration.isEvaluateFormulas()) {
            initializeFormulaEvaluator(workbook);
        }
        convertSheets(workbook, entry.getFileName());
    }

    private Workbook loadWorkbook(FilesDataUnit.Entry entry)
            throws LpException {
        try {
            return WorkbookFactory.create(entry.toFile());
        } catch (IOException | InvalidFormatException ex) {
            throw new LpException("Can't open workbook file.", ex);
        }
    }

    private void initializeFormulaEvaluator(Workbook workbook) {
        FormulaEvaluator evaluator =
                workbook.getCreationHelper().createFormulaEvaluator();
        sheetConverter.setEvaluator(evaluator);
    }

    private void convertSheets(Workbook workbook, String fileName)
            throws LpException {
        for (int index = 0; index < workbook.getNumberOfSheets(); ++index) {
            Sheet sheet = workbook.getSheetAt(index);
            if (!shouldParseSheet(sheet.getSheetName())) {
                continue;
            }
            File output = createOutputFile(fileName, sheet.getSheetName());
            LOG.info("Parsing sheet: '{}' in '{}'", sheet.getSheetName(),
                    fileName);
            convertSheet(sheet, output);
        }
    }

    private boolean shouldParseSheet(String sheetName) {
        if (configuration.getSheetFilter() == null ||
                configuration.getSheetFilter().isEmpty()) {
            return true;
        }
        return sheetName.matches(configuration.getSheetFilter());
    }

    private File createOutputFile(String fileName, String sheetName)
            throws LpException {
        String outputName = getOutputName(fileName, sheetName);
        return outputFiles.createFile(outputName);
    }

    private String getOutputName(String fileName, String sheetName) {
        return configuration.getFileNamePattern().
                replace(ExcelToCsvConfiguration.FILE_HOLDER, fileName).
                replace(ExcelToCsvConfiguration.SHEET_HOLDER, sheetName);
    }

    private void convertSheet(Sheet sheet, File file) throws LpException {
        try (PrintStream outputStream = new PrintStream(
                new FileOutputStream(file), false, "UTF-8")) {
            sheetConverter.convert(sheet, outputStream);
        } catch (IOException ex) {
            throw new LpException("Can't write output to file.", ex);
        }
    }

}
