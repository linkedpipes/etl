package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.util.List;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Default predicate: configuration.url + "#"
 * + URLEncoder.encode(column.name, "UTF-8"));
 *
 *
 */
abstract class ColumnAbstract {

    /**
     * Used to report missing required value.
     */
    public static class MissingColumnValue extends Exception {

        public MissingColumnValue(String name, int rowNumber) {
            super("Missing value for required column: '" + name + "' on row "
                    + Integer.toString(rowNumber));
        }

    }

    public static class MissingNameInHeader extends Exception {

        public MissingNameInHeader(String name) {
            super("Missing column with name '" + name + "'.");
        }

    }

    protected static final ValueFactory VALUE_FACTORY
            = SimpleValueFactory.getInstance();

    protected final String name;

    protected final boolean required;

    protected final ResourceTemplate aboutUrl;

    protected final UrlTemplate predicate;

    private int valueIndex;

    ColumnAbstract(String name, boolean required, ResourceTemplate aboutUrl,
            UrlTemplate predicate) {
        this.name = name;
        this.required = required;
        this.aboutUrl = aboutUrl;
        this.predicate = predicate;
    }

    /**
     * Called before usage should be used to initialize the column template.
     *
     * @param tableUri
     * @param header
     */
    public void initialize(String tableUri, List<String> header)
            throws MissingNameInHeader, InvalidTemplate {
        aboutUrl.initialize(tableUri, header);
        predicate.initialize(tableUri, header);
        valueIndex = -1;
        for (int i = 0; i < header.size(); ++i) {
            if (name.equals(header.get(i))) {
                valueIndex = i;
                break;
            }
        }
        if (valueIndex == -1) {
            throw new MissingNameInHeader(name);
        }
    }

    /**
     *
     * @param outputConsumer
     * @param row
     * @param rowNumber
     * @return Must not return null.
     * @throws NonRecoverableException
     */
    public abstract List<Resource> emit(StatementConsumer outputConsumer,
            List<String> row, int rowNumber)
            throws LpException, MissingColumnValue;

    /**
     * Get value for this column or throw if the column is missing.
     *
     * @param row
     * @param rowNumber
     * @return
     * @throws MissingColumnValue
     */
    protected String getValue(List<String> row, int rowNumber)
            throws MissingColumnValue {
        if (row.size() <= valueIndex) {
            throw new MissingColumnValue(this.name, rowNumber);
        } else {
            return row.get(valueIndex);
        }
    }

}
