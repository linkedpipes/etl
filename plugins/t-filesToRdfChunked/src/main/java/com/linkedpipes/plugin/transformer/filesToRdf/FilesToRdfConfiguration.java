package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FilesToRdfVocabulary.CONFIG)
public class FilesToRdfConfiguration {

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_MIME_TYPE)
    private String mimeType = null;

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_COMMIT_SIZE)
    private int filesPerChunk = 1;

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_SKIP_ON_FAILURE)
    private boolean skipOnFailure = false;

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_FILE_REFERENCE)
    private boolean fileReference = false;

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_FILE_PREDICATE)
    private String filePredicate;

    @RdfToPojo.Property(iri = FilesToRdfVocabulary.HAS_NUMBER_OF_THREADS)
    private int threadCount = 1;

    public FilesToRdfConfiguration() {
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

    public String getFilePredicate() {
        return filePredicate;
    }

    public void setFilePredicate(String filePredicate) {
        this.filePredicate = filePredicate;
    }

    public boolean isFileReference() {
        return fileReference;
    }

    public void setFileReference(boolean fileReference) {
        this.fileReference = fileReference;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

}
