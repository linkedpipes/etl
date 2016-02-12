package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.exception.NonRecoverableException;
import com.linkedpipes.plugin.transformer.tabular.TabularConfiguration.Column;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author Petr Škoda
 */
class ColumnFactory {

    private ColumnFactory() {
    }

    /**
     * Based on given configuration create a list of columns.
     *
     * @param configuration
     * @return
     */
    public static List<ColumnAbstract> createColumnList(TabularConfiguration configuration)
            throws NonRecoverableException {
        final List<ColumnAbstract> result = new ArrayList<>(configuration.getTableSchema().getColumns().size());
        final TabularConfiguration.Schema schema = configuration.getTableSchema();
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();

        final ResourceTemplate defaultAboutUrl = new ResourceTemplate(schema.getAboutUrl());

        for (Column column : schema.getColumns()) {
            // Determine column type - there is no spacial identification so we decide based
            // on parametrs.
            final ResourceTemplate aboutUrl;
            if (column.getAboutUrl() == null || column.getAboutUrl().isEmpty()) {
                aboutUrl = defaultAboutUrl;
            } else {
                aboutUrl = new ResourceTemplate(column.getAboutUrl());
            }

            final UrlTemplate predicate;
            if (column.getPropertyUrl() == null)  {
                throw new NonRecoverableException("Missing predicate for column: '" + column.getName() + "'");
            } else {
                predicate = new UrlTemplate(column.getPropertyUrl());
            }

            if (column.isSupressOutput()) {
                continue;
            }
            if (column.getValueUrl() != null && !column.getValueUrl().isEmpty()) {
                // Column to URL value.
                result.add(new ColumnUrl(new UrlTemplate(column.getValueUrl()), column.getName(), column.isRequired(),
                        aboutUrl, predicate));
            } else if (column.getSeparator() != null) {
                // Column to list.
                throw new UnsupportedOperationException("List is not supported yet!");
            } else if (column.getDatatype() != null) {
                // Column with typed value.
                result.add(new ColumnTyped(valueFactory.createIRI(column.getDatatype()), column.getLang(),
                        column.getName(), column.isRequired(), aboutUrl, predicate));
            } else {
                throw new NonRecoverableException("Invalid configuration for colum: " + column.getName());
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
    public static List<ColumnAbstract> createColumList(TabularConfiguration configuration, List<String> header) {
        final List<ColumnAbstract> result = new ArrayList<>(header.size());
        final TabularConfiguration.Schema schema = configuration.getTableSchema();

        final ResourceTemplate aboutUrl = new ResourceTemplate(schema.getAboutUrl());

        for (String name : header) {
            // Determine column type - there is no spacial identification so we decide based on parametrs.
            final String baseUri;
            if (configuration.isUseBaseUri()) {
                baseUri = configuration.getBaseUri();
            } else {
                baseUri = "{" + StringTemplate.TABLE_RESOURCE_REF + "}#";
            }
            final UrlTemplate predicate = new UrlTemplate(baseUri + encodeString(name));
            // Column with typed value.
            result.add(new ColumnTyped(XMLSchema.STRING, null, name, false, aboutUrl, predicate));
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
