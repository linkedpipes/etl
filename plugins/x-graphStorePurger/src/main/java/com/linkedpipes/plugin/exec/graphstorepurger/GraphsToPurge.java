package com.linkedpipes.plugin.exec.graphstorepurger;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = GraphStorePurgerVocabulary.TASK)
public class GraphsToPurge {

    @RdfToPojo.Property(iri = GraphStorePurgerVocabulary.HAS_GRAPH)
    private List<String> graphs = new LinkedList<>();

    public GraphsToPurge() {
    }

    public List<String> getGraphs() {
        return graphs;
    }

    public void setGraphs(List<String> graphs) {
        this.graphs = graphs;
    }
}
