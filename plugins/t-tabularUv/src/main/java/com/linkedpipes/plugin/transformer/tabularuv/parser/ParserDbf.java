package com.linkedpipes.plugin.transformer.tabularuv.parser;

import com.linkedpipes.etl.component.api.ExecutionFailed;
import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;
import org.jamel.dbf.structure.DbfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdf;
import com.linkedpipes.plugin.transformer.tabularuv.mapper.TableToRdfConfigurator;

/**
 *
 * @author Škoda Petr
 */
public class ParserDbf implements Parser {

    private static final Logger LOG = LoggerFactory.getLogger(ParserDbf.class);

    private final ParserDbfConfig config;

    private final TableToRdf tableToRdf;

    private int rowNumber = 0;

    public ParserDbf(ParserDbfConfig config, TableToRdf tableToRdf) {
        this.config = config;
        this.tableToRdf = tableToRdf;
    }

    @Override
    public void parse(File inFile) throws ExecutionFailed, ParseFailed, NonRecoverableException {
        final String encoding;
        if (config.encoding == null || config.encoding.isEmpty()) {
            // parse from DBF file
            encoding = "UTF-8";
        } else {
            encoding = config.encoding;
        }
        if (!Charset.isSupported(encoding)) {
            throw new ParseFailed("Charset '" + encoding
                    + "' is not supported.");
        }
        final DbfReader reader = new DbfReader(inFile);
        // get header
        final List<String> header;
        final DbfHeader dbfHeader = reader.getHeader();
        header = new ArrayList<>(dbfHeader.getFieldsCount());
        for (int i = 0; i < dbfHeader.getFieldsCount(); ++i) {
            final DbfField field = dbfHeader.getField(i);
            header.add(field.getName());
        }
        // prase other rows
        // set if for first time or if we use static row counter
        if (!config.checkStaticRowCounter || rowNumber == 0) {
            rowNumber = 1;
        }
        int rowNumPerFile = 0;
        Object[] row = reader.nextRecord();
        List<Object> stringRow = new ArrayList(row.length);
        // configure parser
        TableToRdfConfigurator.configure(tableToRdf, header,
                Arrays.asList(row), 0);
        // go ...
        if (config.rowLimit == null) {
            LOG.debug("Row limit: not used");
        } else {
            LOG.debug("Row limit: {}", config.rowLimit);
        }
        while (row != null
                && (config.rowLimit == null || rowNumPerFile < config.rowLimit)) {
            // convert
            for (Object item : row) {
                if (item instanceof byte[]) {
                    try {
                        final String newString
                                = new String((byte[]) item, config.encoding);
                        stringRow.add(newString);
                    } catch (UnsupportedEncodingException ex) {
                        // terminate DPU as this can not be handled
                        throw new RuntimeException(ex);
                    }
                } else {
                    stringRow.add(item);
                }
            }
            // process
            tableToRdf.paserRow(stringRow, rowNumber);
            // read next row
            rowNumber++;
            rowNumPerFile++;
            row = reader.nextRecord();
            stringRow.clear();
            // log
            if ((rowNumPerFile % 1000) == 0) {
                LOG.debug("Row number {} processed.", rowNumPerFile);
            }
        }
    }

}
