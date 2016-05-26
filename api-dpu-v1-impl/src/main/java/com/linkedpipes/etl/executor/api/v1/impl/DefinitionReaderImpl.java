package com.linkedpipes.etl.executor.api.v1.impl;

import com.linkedpipes.etl.dpu.api.service.DefinitionReader;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Škoda
 */
class DefinitionReaderImpl implements DefinitionReader {

    private static final Logger LOG =
            LoggerFactory.getLogger(DefinitionReaderImpl.class);

    private final SparqlSelect sparqlSelect;

    private final String graph;

    private final String componentIri;

    DefinitionReaderImpl(SparqlSelect sparqlSelect, String graph,
            String componentIri) {
        this.sparqlSelect = sparqlSelect;
        this.graph = graph;
        this.componentIri = componentIri;
    }

    @Override
    public Collection<String> getProperty(String predicate) throws OperationFailed  {
        final String query = "SELECT ?value FROM <" + graph +
                ">WHERE { <" + componentIri +
                "> <" + predicate + "> ?value }";
        //
        final List<Map<String, String>> queryResult;
        try {
            queryResult = sparqlSelect.executeSelect(query);
        } catch (SparqlSelect.QueryException ex) {
            throw new OperationFailed("Can't query for data.", ex);
        }
        //
        final List<String> result = new ArrayList<>(2);
        for (Map<String, String> item : queryResult) {
            if (item.containsKey("value")) {
                result.add(item.get("value"));
            }
        }
        return result;
    }

}
