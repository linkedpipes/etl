package com.linkedpipes.plugin.transformer.tabularuv.mapper;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2;
import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2.ColumnInfo_V1;
import com.linkedpipes.plugin.transformer.tabularuv.TabularConfig_V2.ColumnType;
import com.linkedpipes.plugin.transformer.tabularuv.Utils;
import com.linkedpipes.plugin.transformer.tabularuv.column.ValueGeneratorReplace;

import java.util.*;

import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.tabularuv.column.ValueGenerator;
import com.linkedpipes.plugin.transformer.tabularuv.parser.ParseFailed;

/**
 * Configure {@link TableToRdf} class.
 *
 * @author Škoda Petr
 */
public class TableToRdfConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(TableToRdfConfigurator.class);

    private TableToRdfConfigurator() {
    }

    /**
     * Configure given {@link TableToRdf} convertor.
     *
     * @param tableToRdf
     * @param header
     * @param data Contains first data row, or ColumnType if type is already known.
     * @param numberOfLeadingEmpty Number of leading empty columns, this is useful for xsl-like.
     * @throws com.linkedpipes.plugin.transformer.tabularuv.parser.ParseFailed
     * @throws com.linkedpipes.etl.executor.api.v1.exception.LpException
     */
    public static void configure(TableToRdf tableToRdf, List<String> header,
            List<Object> data, int numberOfLeadingEmpty)
            throws ParseFailed, LpException {
        // initial checks
        if (data == null) {
            throw new ParseFailed("First data row is null!");
        }
        if (header != null && header.size() != data.size()) {
            throw new ParseFailed("Diff number of cells in header ("
                    + header.size() + ") and data (" + data.size() + ")");
        }
        //
        final TableToRdfConfig config = tableToRdf.config;
        // clear configuration
        tableToRdf.baseUri = config.baseURI;
        tableToRdf.infoMap = null;
        tableToRdf.keyColumn = null;
        tableToRdf.nameToIndex = new HashMap<>();
        // prepare locals
        Map<String, ColumnInfo_V1> unused = new HashMap<>();
        unused.putAll(config.columnsInfo);
        List<ValueGenerator> valueGenerators = new ArrayList<>(data.size());
        // generate configuration - Column Mapping
        String keyTemplateStr = null;
        for (int index = 0; index < data.size(); index++) {
            // generate column name and add it to map
            final String columnName;
            if (header != null) {
                if (header.get(index) != null) {
                    columnName = header.get(index);
                } else {
                    LOG.info("Generated value used for column with 'null' name.");
                    // use generated one - first is col1, col2 ...
                    columnName = "col" + Integer.toString(index + 1);
                }
            } else {
                // use generated one - first is col1, col2 ...
                columnName = "col" + Integer.toString(index + 1);
            }
            LOG.debug("New column found '{}'", columnName);
            // check for null
            if (columnName == null) {
                LOG.warn("Column with name='null' is ignored.");
                continue;
            }
            // add column name
            tableToRdf.nameToIndex.put(columnName, index);
            // test for key
            if (config.keyColumn != null  && !config.keyColumn.isEmpty()
                    && !config.advancedKeyColumn
                    && config.keyColumn.compareTo(columnName) == 0) {
                // we construct tempalte and use it
                keyTemplateStr = "<" + prepareAsUri("{", config)
                        + columnName + "}>";
            }
            // check for user template
            final ColumnInfo_V1 columnInfo;
            if (config.columnsInfo.containsKey(columnName)) {
                // use user config
                columnInfo = config.columnsInfo.get(columnName);
                unused.remove(columnName);
            } else if (!config.generateNew) {
                // no new generation
                continue;
            } else {
                // generate new
                columnInfo = new ColumnInfo_V1();
            }
            // fill other values if needed
            if (columnInfo.getURI() == null) {
                columnInfo.setURI(config.baseURI
                        + Utils.convertStringToIRIPart(columnName));
            } else {
                columnInfo.setURI(prepareAsUri(columnInfo.getURI(), config));
            }
            if (columnInfo.getType() == ColumnType.Auto) {
                if (index < numberOfLeadingEmpty) {
                    // This is empty leading column, we use string
                    // without warning.
                    columnInfo.setType(ColumnType.String);
                } else if (config.autoAsStrings) {
                    columnInfo.setType(ColumnType.String);
                } else {
                    columnInfo.setType(guessType(columnName, data.get(index),
                            columnInfo.isUseTypeFromDfb()));
                }
            }
            // generate tableToRdf configuration from 'columnInfo'
            final String template = generateTemplate(columnInfo, columnName);
            LOG.debug("Template for column '{}' is '{}'", columnName, template);
            // add to configuration
            valueGenerators.add(ValueGeneratorReplace.create(
                    tableToRdf.valueFactory.createIRI(columnInfo.getURI()),
                    template));
            // generate metadata about column - for now only labels
            if (config.generateLabels) {
                tableToRdf.outRdf.add(
                        tableToRdf.valueFactory.createIRI(columnInfo.getURI()),
                        RDFS.LABEL,
                        tableToRdf.valueFactory.createLiteral(columnName));
            }
        }
        // key template
        if (config.advancedKeyColumn) {
            // we use keyColumn directly
            tableToRdf.keyColumn = ValueGeneratorReplace.create(null,
                    config.keyColumn);
            tableToRdf.keyColumn.compile(tableToRdf.nameToIndex,
                    tableToRdf.valueFactory);
        } else if (keyTemplateStr != null) {
            // we have consructed tempalte
            LOG.info("Key column template: {}", keyTemplateStr);

            tableToRdf.keyColumn = ValueGeneratorReplace.create(null,
                    keyTemplateStr);
            tableToRdf.keyColumn.compile(tableToRdf.nameToIndex,
                    tableToRdf.valueFactory);
        } else {
            // we use null, and then row number is used
        }
        // add columns from user - Template Mapping
        // TODO: we do not support this functionality ..
        for (String key : unused.keySet()) {
            if (key.isEmpty()) {
                // such values got into configuration, so we ignore them
                //     - bug fix
                continue;
            }
            if (config.ignoreMissingColumn) {
                LOG.info("Column '{}' (uri:{}) ignored as does not match "
                        + "original columns.", key, unused.get(key).getURI());
            } else {
                LOG.error("Column '{}' (uri:{}) ignored as does not match "
                        + "original columns.", key, unused.get(key).getURI());
            }
        }
        // add advanced
        for (TabularConfig_V2.AdvanceMapping item : tableToRdf.config.columnsInfoAdv) {
            // prepare URI
            String uri = prepareAsUri(item.getUri(), config);
            // add tempalte
            valueGenerators.add(ValueGeneratorReplace.create(
                    tableToRdf.valueFactory.createIRI(uri),
                    item.getTemplate()));
        }
        // Compile valueGenerators
        for (ValueGenerator generator : valueGenerators) {
            generator.compile(tableToRdf.nameToIndex, tableToRdf.valueFactory);
        }
        // final checks and data sets
        tableToRdf.infoMap = valueGenerators.toArray(new ValueGenerator[0]);
        if (config.rowsClass != null && !config.rowsClass.isEmpty()) {
            try {
                tableToRdf.rowClass
                        = tableToRdf.valueFactory.createIRI(config.rowsClass);
            } catch (IllegalArgumentException ex) {
                throw new ParseFailed("Failed to create row's class URI from:"
                        + config.rowsClass, ex);
            }
        }
    }

    /**
     * Auto type of given value.
     *
     * @param columnName
     * @param value
     * @param useDataType Null is considered to be false.
     * @return
     */
    private static ColumnType guessType(String columnName, Object value,
            Boolean useDataType) {

        if (value instanceof ColumnType) {
            ColumnType type = (ColumnType) value;
            if (type == ColumnType.Auto) {
                throw new RuntimeException("ColumnType.Auto!");
            }
            return type;
        }

        if (useDataType != null && useDataType) {
            if (value instanceof Date) {
                return ColumnType.Date;
            }
            if (value instanceof Float) {
                return ColumnType.Float;
            }
            if (value instanceof Boolean) {
                return ColumnType.Boolean;
            }
            if (value instanceof Number) {
                return ColumnType.Long;
            }
        }
        // Try to parse value
        if (value == null) {
            // we can gues ..
            LOG.warn("Can't determine type for: {} as value in first row is "
                    + "empty, string used as default.", columnName);
            return ColumnType.String;
        }

        final String valueStr = value.toString();
        try {
            Long.parseLong(valueStr);
            return ColumnType.Long;
        } catch (NumberFormatException ex) {

        }
        try {
            Double.parseDouble(valueStr);
            return ColumnType.Double;
        } catch (NumberFormatException ex) {

        }

        // TODO Parse Date
        // use string as default
        return ColumnType.String;
    }

    /**
     * Generate template for given colum.
     *
     * @param columnInfo
     * @param columnName
     * @return
     */
    private static String generateTemplate(ColumnInfo_V1 columnInfo,
            String columnName) {
        // update columnName
        columnName = columnName.replaceAll("\\{", "\\\\{").
                replaceAll("\\}", "\\\\}");

        final String placeHolder = "\"{" + columnName + "}\"";
        switch (columnInfo.getType()) {
            case Boolean:
                return placeHolder + "^^" + XMLSchema.BOOLEAN;
            case Date:
                return placeHolder + "^^" + XMLSchema.DATE;
            case Double:
                return placeHolder + "^^" + XMLSchema.DOUBLE;
            case Float:
                return placeHolder + "^^" + XMLSchema.FLOAT;
            case Integer:
                return placeHolder + "^^" + XMLSchema.INT;
            case Long:
                return placeHolder + "^^" + XMLSchema.LONG;
            case String:
                if (columnInfo.getLanguage() == null
                        || columnInfo.getLanguage().isEmpty()) {
                    return placeHolder;
                } else {
                    return placeHolder + "@" + columnInfo.getLanguage();
                }
            case gYear:
                return placeHolder + "^^" + XMLSchema.GYEAR;
            case Decimal:
                return placeHolder + "^^" + XMLSchema.DECIMAL;
            default:
                LOG.error("No type used for: {}", columnName);
                return placeHolder;

        }
    }

    /**
     * Prepare URI to be used. If given uri is absolute then return it if it's
     * relative then config.baseURI is used to resolve the uri
     *
     * @param uri
     * @param config
     * @return
     */
    private static String prepareAsUri(String uri, TableToRdfConfig config) {
        if (uri.contains("://")) {
            // uri is absolute like http://, file://
            return uri;
        } else {
            final String newUri;
            // uri is relative, concat with base URI,
            // just be carefull to /
            if (uri.startsWith("/")) {
                if (config.baseURI.endsWith("/")) {
                    // both have /
                    newUri = config.baseURI + uri.substring(1);
                } else {
                    // just one has /
                    newUri = config.baseURI + uri;
                }
            } else if (config.baseURI.endsWith("/")) {
                // just one has /
                newUri = config.baseURI + uri;
            } else {
                // one one has /
                newUri = config.baseURI + "/" + uri;
            }
            LOG.debug("URI '{}' -> '{}'", uri, newUri);
            return newUri;
        }
    }

}
