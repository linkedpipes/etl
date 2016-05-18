package com.linkedpipes.plugin.transformer.tabularuv.column;

import java.util.List;

import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 *
 *
 * @author Škoda Petr
 */
public class ValueGeneratorUri extends ValueGeneratorReplace {

    public ValueGeneratorUri(IRI uri, String template) {
        super(uri, template);
    }

    @Override
    public Value generateValue(List<Object> row, ValueFactory valueFactory) {
        final String rawResult = super.process(row);
        if (rawResult == null) {
            return null;
        }
        // the replace thing is done as a part of ValueGeneratorReplace
        return valueFactory.createIRI(rawResult);
    }

}
