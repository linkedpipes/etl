package com.linkedpipes.plugin.extractor.local;

import com.linkedpipes.etl.dpu.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = FilesFromLocalVocabulary.CONFIG_CLASS)
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
