package com.linkedpipes.plugin.transformer.jsonldtofile;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonLdToRdfChunkedVocabulary.CONFIG)
public class JsonLdToRdfChunkedConfiguration {

    @RdfToPojo.Property(iri = JsonLdToRdfChunkedVocabulary.HAS_COMMIT_SIZE)
    private int filesPerChunk = 1;

    @RdfToPojo.Property(iri = JsonLdToRdfChunkedVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

    @RdfToPojo.Property(iri = JsonLdToRdfChunkedVocabulary.HAS_FILE_REFERENCE)
    private boolean fileReference = false;

    @RdfToPojo.Property(iri = JsonLdToRdfChunkedVocabulary.HAS_FILE_PREDICATE)
    private String filePredicate;

    public JsonLdToRdfChunkedConfiguration() {

    }

    public int getFilesPerChunk() {
        return filesPerChunk;
    }

    public void setFilesPerChunk(int filesPerChunk) {
        this.filesPerChunk = filesPerChunk;
    }

    public boolean isSkipOnFailure() {
        return skipOnFailure;
    }

    public void setSkipOnFailure(boolean skipOnFailure) {
        this.skipOnFailure = skipOnFailure;
    }

    public boolean isFileReference() {
        return fileReference;
    }

    public void setFileReference(boolean fileReference) {
        this.fileReference = fileReference;
    }

    public String getFilePredicate() {
        return filePredicate;
    }

    public void setFilePredicate(String filePredicate) {
        this.filePredicate = filePredicate;
    }

}
