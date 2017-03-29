package com.linkedpipes.plugin.transformer.sparql.constructtofilelist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlConstructToFileListVocabulary.TASK_QUERY)
public class QueryTask {

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_GRAPH_IRI)
    private String graph;

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_QUERY)
    private String query;

    public QueryTask() {
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
