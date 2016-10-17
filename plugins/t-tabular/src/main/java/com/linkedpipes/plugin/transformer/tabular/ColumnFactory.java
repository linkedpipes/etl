package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.component.api.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.plugin.transformer.tabular.TabularConfiguration.Column;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Petr Škoda
 */
class ColumnFactory {

    private static final Logger LOG
            = LoggerFactory.getLogger(ColumnFactory.class);

    private ColumnFactory() {
    }

    /**
     * Based on given configuration create a list of columns.
     *
     * @param configuration
     * @return
     */
    public static List<ColumnAbstract> createColumnList(
            TabularConfiguration configuration,
            ExceptionFactory exceptionFactory) throws LpException {
        // The configuration can contains user mapping, but if the full
        // mapping is used all user options are ignored.
        if (configuration.isFullMapping()) {
            return Collections.EMPTY_LIST;
        }

        final List<ColumnAbstract> result = new ArrayList<>(
                configuration.getTableSchema().getColumns().size());
        final TabularConfiguration.Schema schema
                = configuration.getTableSchema();
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        final ResourceTemplate defaultAboutUrl
                = new ResourceTemplate(schema.getAboutUrl());

        for (Column column : schema.getColumns()) {
            // Determine column type - there is no spacial identification
            // so we decide based on parametrs.
            final ResourceTemplate aboutUrl;
            if (column.getAboutUrl() == null
                    || column.getAboutUrl().isEmpty()) {
                aboutUrl = defaultAboutUrl;
            } else {
                aboutUrl = new ResourceTemplate(column.getAboutUrl());
            }

            final UrlTemplate predicate;
            if (column.getPropertyUrl() == null) {
                throw exceptionFactory.failure(
                        "Missing predicate for column: '{}'", column.getName());
            } else {
                // We need to test if we got absolute IRI or not.
                String predicateAsString = column.getPropertyUrl();
                try {
                    if (!URI.create(predicateAsString).isAbsolute()) {
                        predicateAsString = configuration.getBaseUri()
                                + predicateAsString;
                    }
                } catch (IllegalArgumentException ex) {
                    throw exceptionFactory.failure("Invalid IRI: '{}'",
                            ex);
                }
                predicate = new UrlTemplate(predicateAsString);
            }

            if (column.isSupressOutput()) {
                continue;
            }
            if (column.getValueUrl() != null
                    && !column.getValueUrl().isEmpty()) {
                // Column to URL value.
                result.add(new ColumnUrl(new UrlTemplate(column.getValueUrl()),
                        column.getName(), column.isRequired(),
                        aboutUrl, predicate));
            } else if (column.getSeparator() != null) {
                // Column to list.
                throw new UnsupportedOperationException(
                        "List is not supported yet!");
            } else if (column.getDatatype() != null) {
                final IRI type;
                try {
                    type = valueFactory.createIRI(column.getDatatype());
                } catch (RuntimeException ex) {
                    throw exceptionFactory.failure(
                            "Invalid column type '{}' for colum: '{}'",
                            column.getDatatype(), column.getName(), ex);
                }
                // Column with typed value.
                result.add(new ColumnTyped(type, column.getLang(),
                        column.getName(), column.isRequired(),
                        aboutUrl, predicate));
            } else {
                throw exceptionFactory.failure(
                        "Invalid configuration for column {}", column.getName());
            }
        }
        return result;
    }

    /**
     * Used to create default columns.
     *
     * @param configuration
     * @param header Data header.
     * @return
     */
    public static List<ColumnAbstract> createColumList(
            TabularConfiguration configuration, List<String> header,
            ExceptionFactory exceptionFactory) throws LpException {
        final List<ColumnAbstract> result = new ArrayList<>(header.size());
        final TabularConfiguration.Schema schema
                = configuration.getTableSchema();
        final ResourceTemplate aboutUrl
                = new ResourceTemplate(schema.getAboutUrl());
        // MissingNameInHeader
        int counter = 0;
        for (String name : header) {
            counter += 1;
            // Determine column type - there is no spacial identification
            // so we decide based on parametrs.
            final String baseUri;
            if (configuration.isUseBaseUri()) {
                baseUri = configuration.getBaseUri();
            } else {
                baseUri = "{" + StringTemplate.TABLE_RESOURCE_REF + "}#";
            }
            if (name == null) {
                if (configuration.isGenerateNullHeaderName()) {
                    name = "generated_name_" + Integer.toString(counter);
                    header.set(counter - 1, name);
                } else {
                    LOG.info("Header: {}", header);
                    throw exceptionFactory.failure(
                            "Header must not contains null values.");
                }
            }
            final UrlTemplate predicate
                    = new UrlTemplate(baseUri + encodeString(name));
            // Column with typed value.
            result.add(new ColumnTyped(XMLSchema.STRING, null, name, false,
                    aboutUrl, predicate));
        }
        return result;
    }

    private static String encodeString(String part) {
        try {
            return URLEncoder.encode(part, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding", ex);
        }
    }

}
