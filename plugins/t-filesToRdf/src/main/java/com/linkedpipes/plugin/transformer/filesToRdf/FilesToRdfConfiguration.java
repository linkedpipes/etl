package com.linkedpipes.plugin.transformer.filesToRdf;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = FilesToRdfVocabulary.CONFIG)
public class FilesToRdfConfiguration {

    @RdfToPojo.Property(uri = FilesToRdfVocabulary.HAS_COMMIT_SIZE)
    private int commitSize = 10000;

    @RdfToPojo.Property(uri = FilesToRdfVocabulary.HAS_MIME_TYPE)
    private String mimeType = null;

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

}
