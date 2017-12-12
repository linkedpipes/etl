package com.linkedpipes.etl.executor.api.v1.service;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfSource;

import java.util.Collection;
import java.util.stream.Collectors;

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
        return definition.getPropertyValues(graph, component, property)
                .stream()
                .map((item) -> (item.asString()))
                .collect(Collectors.toList());
    }

}


