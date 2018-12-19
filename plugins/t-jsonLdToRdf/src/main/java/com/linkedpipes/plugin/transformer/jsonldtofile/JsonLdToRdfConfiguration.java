package com.linkedpipes.plugin.transformer.jsonldtofile;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonLdToRdfVocabulary.CONFIG)
public class JsonLdToRdfConfiguration {

    @RdfToPojo.Property(iri = JsonLdToRdfVocabulary.HAS_COMMIT_SIZE)
    private int commitSize = 10000;

    @RdfToPojo.Property(iri = JsonLdToRdfVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

    public JsonLdToRdfConfiguration() {

    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }

    public boolean isSkipOnFailure() {
        return skipOnFailure;
    }

    public void setSkipOnFailure(boolean skipOnFailure) {
        this.skipOnFailure = skipOnFailure;
    }

}
