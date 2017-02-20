package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.rdf.utils.RdfSource;
import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class DefaultDefinitionReader implements DefinitionReader {

    private final String component;

    private final String graph;

    private final RdfSource definition;

    public DefaultDefinitionReader(String component, String graph,
            RdfSource definition) {
        this.component = component;
        this.graph = graph;
        this.definition = definition;
    }

    @Override
    public Collection<String> getProperties(String property)
            throws LpException {
        final List<Map<String, String>> bindings = queryForProperty(property);
        final List<String> result = new ArrayList<>(bindings.size());
        for (Map<String, String> entry : bindings) {
            result.add(entry.get("value"));
        }
        return result;
    }

    private List<Map<String, String>> queryForProperty(String property)
            throws LpException {
        try {
            return definition.sparqlSelect(
                    getQueryForProperty(property), String.class);
        } catch (RdfUtilsException ex) {
            throw new LpException("Can't query source.", ex);
        }
    }

    private String getQueryForProperty(String property) {
        return "SELECT ?value FROM <" + graph + "> WHERE {" +
                " <" + component + "> <" + property + "> ?value . " +
                "}";
    }

}


