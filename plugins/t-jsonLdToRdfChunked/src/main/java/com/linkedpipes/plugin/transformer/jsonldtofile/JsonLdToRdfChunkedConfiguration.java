package com.linkedpipes.plugin.transformer.jsonldtofile;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonLdToRdfChunkedVocabulary.CONFIG)
public class JsonLdToRdfChunkedConfiguration {

    @RdfToPojo.Property(iri = JsonLdToRdfChunkedVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

    public JsonLdToRdfChunkedConfiguration() {

    }

    public boolean isSkipOnFailure() {
        return skipOnFailure;
    }

    public void setSkipOnFailure(boolean skipOnFailure) {
        this.skipOnFailure = skipOnFailure;
    }

}
