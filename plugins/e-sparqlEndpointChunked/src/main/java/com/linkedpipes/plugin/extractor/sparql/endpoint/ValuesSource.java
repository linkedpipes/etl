package com.linkedpipes.plugin.extractor.sparql.endpoint;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final Set<String> literals;

    public ValuesSource(
            File inputFile,
            ExceptionFactory exceptionFactory,
            List<String> literals,
            int chunkSize) {
        this.inputFile = inputFile;
        this.exceptionFactory = exceptionFactory;
        this.chunkSize = chunkSize;
        this.literals = new HashSet<>(literals);
    }

    public void readSource(ValueHandler handler) throws LpException {
        try (FileInputStream fileInputStream
                     = new FileInputStream(inputFile);
             InputStreamReader inputStreamReader
                     = new InputStreamReader(fileInputStream, "UTF-8");
             CsvListReader csvReader
                     = new CsvListReader(inputStreamReader, CSV_PREFERENCE)) {
            List<String> header = csvReader.read();
            List<List<String>> rows = new ArrayList<>(chunkSize);
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

    private void handle(
            List<String> header, List<List<String>> rows, ValueHandler handler)
            throws LpException {
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
            for (int i = 0; i < row.size(); ++i) {
                String value = row.get(i);
                builder.append(addQuotes(value, header.get(i)));
            }
            builder.append(" ) \n");
        }
        builder.append(" } \n");
        handler.handle(builder.toString());
    }

    private String addQuotes(String value, String name) {
        if (value == null) {
            return " UNDEF ";
        }
        if (this.literals.contains(name)) {
            return " \"" + value + "\"";
        } else {
            return " <" + value + ">";
        }
    }

}
