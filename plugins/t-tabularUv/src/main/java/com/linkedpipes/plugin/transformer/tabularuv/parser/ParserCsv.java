package com.linkedpipes.plugin.transformer.tabularuv.parser;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.io.*;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.QuoteMode;
import org.supercsv.util.CsvContext;

import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdf;
import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdfConfigurator;

/**
 * Parse csv file.
 *
 * @author Škoda Petr
 */
public class ParserCsv implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(ParserCsv.class);

    private final ParserCsvConfig config;

    private final TableToRdf tableToRdf;


    private int rowNumber = 0;

    public ParserCsv(ParserCsvConfig config, TableToRdf tableToRdf) {
        this.config = config;
        this.tableToRdf = tableToRdf;
    }

    @Override
    public void parse(File inFile) throws ParseFailed, LpException {
        final CsvPreference csvPreference;
        // We will use quates only if they are provided
        if (config.quoteChar == null || config.quoteChar.isEmpty()) {
            // We do not use quates.
            final QuoteMode customQuoteMode = (String csvColumn,
                    CsvContext ctx, CsvPreference preference) -> {
                return false;
            };
            // Quate char is never used.
            csvPreference = new CsvPreference.Builder(' ',
                    config.delimiterChar,
                    "\\n").useQuoteMode(customQuoteMode).build();
        } else {
            csvPreference = new CsvPreference.Builder(
                    config.quoteChar.charAt(0),
                    config.delimiterChar,
                    "\\n").build();
        }
        if (!config.checkStaticRowCounter || rowNumber == 0) {
            rowNumber = config.hasHeader ? 2 : 1;
        }
        try (FileInputStream fileInputStream = new FileInputStream(inFile);
                InputStreamReader inputStreamReader = getInputStream(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                CsvListReader csvListReader = new CsvListReader(bufferedReader, csvPreference)) {
            // ignore initial ? lines
            for (int i = 0; i < config.numberOfStartLinesToIgnore; ++i) {
                bufferedReader.readLine();
            }
            // get header
            final List<String> header;
            if (config.hasHeader) {
                header = Arrays.asList(csvListReader.getHeader(true));
            } else {
                header = null;
            }
            // read rows and parse
            int rowNumPerFile = 0;
            List<String> row = csvListReader.read();
            if (row == null) {
                // no data
                LOG.info("No data found!");
                return;
            }
            // configure parser
            TableToRdfConfigurator.configure(tableToRdf, header, (List) row, 0);
            // go ...
            if (config.rowLimit == null) {
                LOG.debug("Row limit: not used");
            } else {
                LOG.debug("Row limit: {}", config.rowLimit);
            }
            while (row != null && (config.rowLimit == null
                    || rowNumPerFile < config.rowLimit)) {
                // cast string to objects
                tableToRdf.paserRow((List) row, rowNumber);
                // read next row
                rowNumber++;
                rowNumPerFile++;
                row = csvListReader.read();
                // log
                if ((rowNumPerFile % 5000) == 0) {
                    LOG.debug("Row number {} processed.", rowNumPerFile);
                }
            }
        } catch (IOException ex) {
            throw new ParseFailed("Parse of '" + inFile.toString()
                    + "' failure", ex);
        }
    }

    /**
     * Create {@link InputStreamReader}. If "UTF-8" as encoding is given
     * then {@link BOMInputStream} is used as intermedian between given
     * fileInputStream and output {@link InputStreamReader} to remove possible
     * BOM mark at the start of "UTF" files.
     *
     * @param fileInputStream
     * @return
     * @throws UnsupportedEncodingException
     */
    private InputStreamReader getInputStream(FileInputStream fileInputStream)
            throws UnsupportedEncodingException {
        if (config.encoding.compareToIgnoreCase("UTF-8") == 0) {
            return new InputStreamReader(
                    new BOMInputStream(fileInputStream, false), config.encoding);
        } else {
            return new InputStreamReader(fileInputStream, config.encoding);
        }
    }

}
