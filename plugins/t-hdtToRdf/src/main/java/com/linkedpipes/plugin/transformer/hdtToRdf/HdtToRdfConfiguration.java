package com.linkedpipes.plugin.transformer.hdtToRdf;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = HdtToRdfVocabulary.CONFIG)
public class HdtToRdfConfiguration {

    @RdfToPojo.Property(iri = HdtToRdfVocabulary.HAS_COMMIT_SIZE)
    private int commitSize = 10000;

    public HdtToRdfConfiguration() {
    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }

}
