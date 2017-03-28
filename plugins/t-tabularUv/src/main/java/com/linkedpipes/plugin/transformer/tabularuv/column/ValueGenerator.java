package com.linkedpipes.plugin.transformer.tabularuv.column;

import com.linkedpipes.plugin.transformer.tabularuv.parser.ParseFailed;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

import java.util.List;
import java.util.Map;

public interface ValueGenerator {

    /**
     * Prepare {@link ValueGenerator} to use.
     *
     * @param nameToIndex Mapping from names to indexes in row.
     * @param valueFactory
     */
    void compile(Map<String, Integer> nameToIndex,
            ValueFactory valueFactory) throws ParseFailed;

    /**
     * Generate value based on stored information.
     *
     * @param row
     * @param valueFactory
     * @return
     */
    Value generateValue(List<Object> row, ValueFactory valueFactory);

    /**
     * @return IRI for generated value.
     */
    IRI getUri();

}
