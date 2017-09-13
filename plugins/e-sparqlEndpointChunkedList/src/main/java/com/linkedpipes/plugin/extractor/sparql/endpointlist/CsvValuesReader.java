package com.linkedpipes.plugin.extractor.sparql.endpointlist;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class CsvValuesReader {

    @FunctionalInterface
    public interface ValueConsumer {

        void accept(String valuesClause) throws LpException;

    }

    private final CsvPreference CSV_PREFERENCE
            = new CsvPreference.Builder('"', ',', "\\n").build();

    private final ExceptionFactory exceptionFactory;

    private final int chunkSize;

    private ValueConsumer handler;

    private List<String> header;

    public CsvValuesReader(ExceptionFactory exceptionFactory, int chunkSize) {
        this.exceptionFactory = exceptionFactory;
        this.chunkSize = chunkSize;
    }

    public void setHandler(ValueConsumer handler) {
        this.handler = handler;
    }

    public void readFile(File inputFile) throws LpException {
        try (FileInputStream fileInputStream
                     = new FileInputStream(inputFile);
             InputStreamReader inputStreamReader
                     = new InputStreamReader(fileInputStream, "UTF-8");
             CsvListReader csvReader
                     = new CsvListReader(inputStreamReader, CSV_PREFERENCE)) {
            readCsvTable(csvReader);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't read input file.", ex);
        }
    }

    private void readCsvTable(CsvListReader csvReader)
            throws IOException, LpException {
        this.header = csvReader.read();
        List<List<String>> rows = new ArrayList<>(chunkSize);
        List<String> row = csvReader.read();
        while (row != null) {
            rows.add(row);
            row = csvReader.read();
            if (rows.size() >= chunkSize) {
                handleRows(rows);
                rows.clear();
            }
        }
        if (!rows.isEmpty()) {
            handleRows(rows);
        }
    }

    private void handleRows(List<List<String>> rows) throws LpException {
        handler.accept(buildSparqlValuesClause(rows));
    }

    private String buildSparqlValuesClause(List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("VALUES (");
        for (String s : header) {
            builder.append("?");
            builder.append(s);
            builder.append(" ");
        }
        builder.append(" ) \n {");
        for (List<String> row : rows) {
            builder.append("  (");
            for (String s : row) {
                builder.append(" <");
                builder.append(s);
                builder.append(">");
            }
            builder.append(" ) \n");
        }
        builder.append(" } \n");
        return builder.toString();
    }

}
