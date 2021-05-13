package com.linkedpipes.plugin.transformer.jsonldtofile.titanium;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonLdToRdfTitaniumVocabulary.CONFIG)
public class JsonLdToRdfTitaniumConfiguration {

    @RdfToPojo.Property(iri = JsonLdToRdfTitaniumVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

    public JsonLdToRdfTitaniumConfiguration() {

    }

    public boolean isSkipOnFailure() {
        return skipOnFailure;
    }

    public void setSkipOnFailure(boolean skipOnFailure) {
        this.skipOnFailure = skipOnFailure;
    }

}
