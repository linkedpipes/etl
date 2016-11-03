package com.linkedpipes.plugin.extractor.local;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 */
@RdfToPojo.Type(uri = FilesFromLocalVocabulary.CONFIG)
public class FilesFromLocalConfiguration {

    @RdfToPojo.Property(uri = FilesFromLocalVocabulary.HAS_PATH)
    private String path;

    public FilesFromLocalConfiguration() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
