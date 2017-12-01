package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FilesToRdfVocabulary.CONFIG)
public class FilesToRdfConfiguration {

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_COMMIT_SIZE)
    private int commitSize = 10000;

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_MIME_TYPE)
    private String mimeType = null;

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

    public FilesToRdfConfiguration() {
    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isSkipOnFailure() {
        return skipOnFailure;
    }

    public void setSkipOnFailure(boolean skipOnFailure) {
        this.skipOnFailure = skipOnFailure;
    }

}
