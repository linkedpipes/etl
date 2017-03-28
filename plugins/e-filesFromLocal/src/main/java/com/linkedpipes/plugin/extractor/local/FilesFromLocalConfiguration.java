package com.linkedpipes.plugin.extractor.local;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FilesFromLocalVocabulary.CONFIG)
public class FilesFromLocalConfiguration {

    @RdfToPojo.Property(iri = FilesFromLocalVocabulary.HAS_PATH)
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
