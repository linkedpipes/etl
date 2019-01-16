package com.linkedpipes.plugin.transformer.sparql.constructtofilelist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlConstructToFileListVocabulary.CONFIG)
public class SparqlConstructToFileListConfiguration {

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_DEDUPLICATION)
    private boolean useDeduplication = false;

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_PREFIX_TTL)
    private String prefixTurtle = "";

    public SparqlConstructToFileListConfiguration() {
    }

    public boolean isUseDeduplication() {
        return useDeduplication;
    }

    public void setUseDeduplication(boolean useDeduplication) {
        this.useDeduplication = useDeduplication;
    }

    public String getPrefixTurtle() {
        return prefixTurtle;
    }

    public void setPrefixTurtle(String prefixTurtle) {
        this.prefixTurtle = prefixTurtle;
    }

}