package com.linkedpipes.etl.component.api.impl;

import com.linkedpipes.etl.component.api.service.DefinitionReader;
import com.linkedpipes.etl.executor.api.v1.RdfException;
import com.linkedpipes.etl.executor.api.v1.exception.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.SparqlSelect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class DefinitionReaderImpl implements DefinitionReader {

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
    public Collection<String> getProperty(String predicate) throws LpException {
        final String query = createQuery(graph, componentIri, predicate);
        //
        final List<Map<String, String>> queryResult;
        try {
            queryResult = sparqlSelect.executeSelect(query);
        } catch (RdfException ex) {
            throw RdfException.failure("Can't query for data.", ex);
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

    private String createQuery(String graph, String resource,
            String predicate) {
        return "SELECT ?value FROM <" + graph
                + "> WHERE { <" + resource + "> <"
                + predicate + "> ?value }";
    }

}
