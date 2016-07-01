package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

/**
 * Represent a column with a type.
 *
 * @author Petr Škoda
 */
class ColumnTyped extends ColumnAbstract {

    private final IRI type;

    /**
     * Is used only if type is a string.
     */
    private final String language;

    ColumnTyped(IRI type, String language, String name, boolean required,
            ResourceTemplate aboutUrl, UrlTemplate predicate) {
        super(name, required, aboutUrl, predicate);
        this.type = type;
        this.language = language;
    }

    @Override
    public List<Resource> emit(StatementConsumer outputConsumer,
            List<String> row, int rowNumber)
            throws LpException, MissingColumnValue {
        final Resource s = aboutUrl.getResource(row, rowNumber);
        if (s == null) {
            return Collections.EMPTY_LIST;
        }
        final String valueAsString = getValue(row, rowNumber);
        if (valueAsString == null) {
            // TODO Add empty URI?
            return Collections.EMPTY_LIST;
        }
        final Value o;
        if (language == null || language.isEmpty()) {
            o = VALUE_FACTORY.createLiteral(valueAsString, type);
        } else {
            o = VALUE_FACTORY.createLiteral(valueAsString, language);
        }
        final IRI p = predicate.getUrl(row, rowNumber);
        if (p == null) {
            return Collections.EMPTY_LIST;
        }
        outputConsumer.submit(s, p, o);
        return Arrays.asList(s);
    }

}
