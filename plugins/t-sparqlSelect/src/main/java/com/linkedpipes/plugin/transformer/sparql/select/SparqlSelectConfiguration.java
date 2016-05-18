package com.linkedpipes.plugin.transformer.sparql.select;

import com.linkedpipes.etl.dpu.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = SparqlSelectVocabulary.CONFIG_CLASS)
public class SparqlSelectConfiguration {

    @RdfToPojo.Property(uri = SparqlSelectVocabulary.CONFIG_SPARQL)
    private String query = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(uri = SparqlSelectVocabulary.OUTPUT_FILE_NAME)
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
