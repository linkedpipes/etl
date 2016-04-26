package com.linkedpipes.plugin.transformer.tabularuv.column;

import java.util.List;
import java.util.Map;

import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import com.linkedpipes.plugin.transformer.tabularuv.parser.ParseFailed;

/**
 *
 * @author Škoda Petr
 */
public class ValueGeneratorTyped extends ValueGeneratorReplace {

    private final String typeStr;

    private IRI typeUri;

    public ValueGeneratorTyped(IRI uri, String template, String typeStr) {
        super(uri, template);
        this.typeStr = typeStr;
    }

    @Override
    public Value generateValue(List<Object> row, ValueFactory valueFactory) {
        final String rawResult = super.process(row);
        if (rawResult == null) {
            return null;
        }

        return valueFactory.createLiteral(rawResult, typeUri);
    }

    @Override
    public void compile(Map<String, Integer> nameToIndex,
            ValueFactory valueFactory) throws ParseFailed {
        super.compile(nameToIndex, valueFactory);
        typeUri = valueFactory.createIRI(typeStr);
    }

}
