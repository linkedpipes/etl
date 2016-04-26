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
public interface ValueGenerator {

    /**
     * Prepare {@link ValueGenerator} to use.
     *
     * @param nameToIndex Mapping from names to indexes in row.
     * @param valueFactory
     * @throws com.linkedpipes.plugin.transformer.tabularuv.parser.ParseFailed
     */
    public void compile(Map<String, Integer> nameToIndex,
            ValueFactory valueFactory) throws ParseFailed;

    /**
     * Generate value based on stored information.
     *
     * @param row
     * @param valueFactory
     * @return
     */
    public Value generateValue(List<Object> row, ValueFactory valueFactory);

    /**
     *
     * @return IRI for generated value.
     */
    public IRI getUri();

}
