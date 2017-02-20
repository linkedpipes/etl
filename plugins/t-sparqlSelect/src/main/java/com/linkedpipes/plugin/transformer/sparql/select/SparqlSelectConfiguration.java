package com.linkedpipes.plugin.transformer.sparql.select;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlSelectVocabulary.CONFIG)
public class SparqlSelectConfiguration {

    @RdfToPojo.Property(iri = SparqlSelectVocabulary.HAS_QUERY)
    private String query = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(iri = SparqlSelectVocabulary.HAS_FILE_NAME)
    private String fileName;

    public SparqlSelectConfiguration() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
