package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.dataunit.system.api.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.plugin.transformer.tabular.ColumnAbstract.MissingNameInHeader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.QuoteMode;
import org.supercsv.util.CsvContext;

/**
 *
 * @author Petr Škoda
 */
class Parser {

    private static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private final TabularConfiguration.Dialect dialect;

    private final CsvPreference csvPreference;

    private final ExceptionFactory exceptionFactory;

    Parser(TabularConfiguration configuration,
            ExceptionFactory exceptionFactory) {
        this.dialect = configuration.getDialect();
        this.exceptionFactory = exceptionFactory;
        // We will use quates only if they are provided
        if (dialect.getQuoteChar() == null || dialect.getQuoteChar().isEmpty()) {
            // We do not use quates.
            final QuoteMode customQuoteMode = (String csvColumn,
                    CsvContext context, CsvPreference preference) -> false;
            // Quote char is never used.
            csvPreference = new CsvPreference.Builder(' ',
                    dialect.getDelimeter().charAt(0),
                    "\\n").useQuoteMode(customQuoteMode).build();
            // Line terminators are also part of the configuration!
        } else {
            csvPreference = new CsvPreference.Builder(
                    dialect.getQuoteChar().charAt(0),
                    dialect.getDelimeter().charAt(0),
                    "\\n").build();
        }
    }

    public void parse(FilesDataUnit.Entry entry, Mapper mapper)
            throws UnsupportedEncodingException, IOException, LpException,
            ColumnAbstract.MissingColumnValue {
        try (final FileInputStream fileInputStream
                = new FileInputStream(entry.toFile());
                final InputStreamReader inputStreamReader
                = getInputStream(fileInputStream);
                final BufferedReader bufferedReader
                = new BufferedReader(inputStreamReader);
                final CsvListReader csvListReader
                = new CsvListReader(bufferedReader, csvPreference)) {
            List<String> header;
            List<String> row;
            if (dialect.isHeader()) {
                header = Arrays.asList(csvListReader.getHeader(true));
                row = csvListReader.read();
                // TODO Should we really trim header?
                if (dialect.isTrim()) {
                    header = trimList(header);
                }
            } else {
                row = csvListReader.read();
                // We use row size to create artificial header.
                // This is not according to specification
                // where they always have header.
                header = new ArrayList<>(row.size());
                for (int i = 1; i <= row.size(); i++) {
                    header.add("column_" + Integer.toString(i));
                }
            }
            try {
                mapper.onHeader(header);
            } catch (InvalidTemplate | MissingNameInHeader ex) {
                throw exceptionFactory.failed("Can initalize on header row.",
                        ex);
            }
            if (row == null) {
                LOG.info("No data found in file: {}", entry.getFileName());
                return;
            }
            while (row != null) {
                if (dialect.isTrim()) {
                    row = trimList(row);
                }
                if (!mapper.onRow(row)) {
                    break;
                }
                row = csvListReader.read();
            }
        }
    }

    private static List<String> trimList(List<String> row) {
        final List<String> trimmedRow = new ArrayList<>(row.size());
        for (String item : row) {
            if (item != null) {
                item = item.trim();
            }
            trimmedRow.add(item);
        }
        return trimmedRow;
    }

    /**
     * Create {@link InputStreamReader}. If "UTF-8" as encoding is given then
     * {@link BOMInputStream} is used as wrap of given fileInputStream
     * and output {@link InputStreamReader} to remove possible
     * BOM mark at the start of "UTF" files.
     *
     * @param fileInputStream
     * @return
     * @throws UnsupportedEncodingException
     */
    private InputStreamReader getInputStream(FileInputStream fileInputStream)
            throws UnsupportedEncodingException {
        if (dialect.getEncoding().compareToIgnoreCase("UTF-8") == 0) {
            return new InputStreamReader(
                    new BOMInputStream(fileInputStream, false),
                    dialect.getEncoding());
        } else {
            return new InputStreamReader(fileInputStream,
                    dialect.getEncoding());
        }
    }

}
