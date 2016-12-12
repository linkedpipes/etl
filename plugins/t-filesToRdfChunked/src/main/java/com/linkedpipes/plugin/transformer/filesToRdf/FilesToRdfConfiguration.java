package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

@RdfToPojo.Type(uri = FilesToRdfVocabulary.CONFIG)
public class FilesToRdfConfiguration {

    @RdfToPojo.Property(uri = FilesToRdfVocabulary.HAS_MIME_TYPE)
    private String mimeType = null;

    @RdfToPojo.Property(uri = FilesToRdfVocabulary.HAS_COMMIT_SIZE)
    private int filesPerChunk = 1;

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
}
