package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Designed to be source of VALUES clauses.
 */
public class ValuesSource {

    public interface ValueHandler {

        void handle(String valuesClause) throws LpException;

    }

    private final CsvPreference CSV_PREFERENCE = new CsvPreference.Builder(
            '"', ',', "\\n").build();

    private final File inputFile;

    private final ExceptionFactory exceptionFactory;

    private final int chunkSize;

    public ValuesSource(File inputFile, ExceptionFactory exceptionFactory,
            int chunkSize) {
        this.inputFile = inputFile;
        this.exceptionFactory = exceptionFactory;
        this.chunkSize = chunkSize;
    }

    public void readSource(ValueHandler handler) throws LpException {
        try (final FileInputStream fileInputStream
                     = new FileInputStream(inputFile);
             final InputStreamReader inputStreamReader
                     = new InputStreamReader(fileInputStream, "UTF-8");
             final CsvListReader csvReader
                     = new CsvListReader(inputStreamReader, CSV_PREFERENCE)) {
            final List<String> header = csvReader.read();
            final List<List<String>> rows = new ArrayList<>(chunkSize);
            List<String> row = csvReader.read();
            while (row != null) {
                rows.add(row);
                row = csvReader.read();
                if (rows.size() >= chunkSize) {
                    handle(header, rows, handler);
                    rows.clear();
                }
            }
            if (!rows.isEmpty()) {
                handle(header, rows, handler);
            }
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't read input file.", ex);
        }

    }

    protected void handle(List<String> header, List<List<String>> rows,
            ValueHandler handler) throws LpException {
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
        handler.handle(builder.toString());
    }

}
