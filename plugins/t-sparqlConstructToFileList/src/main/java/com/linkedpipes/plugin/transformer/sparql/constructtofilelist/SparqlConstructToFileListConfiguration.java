package com.linkedpipes.plugin.transformer.sparql.constructtofilelist;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlConstructToFileListVocabulary.CONFIG)
public class SparqlConstructToFileListConfiguration {

    @RdfToPojo.Property(iri = SparqlConstructToFileListVocabulary.HAS_DEDUPLICATION)
    private boolean useDeduplication = false;

    public SparqlConstructToFileListConfiguration() {
    }

    public boolean isUseDeduplication() {
        return useDeduplication;
    }

    public void setUseDeduplication(boolean useDeduplication) {
        this.useDeduplication = useDeduplication;
    }

}