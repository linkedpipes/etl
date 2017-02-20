package com.linkedpipes.plugin.transformer.tabularuv.column;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

import java.util.List;

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
