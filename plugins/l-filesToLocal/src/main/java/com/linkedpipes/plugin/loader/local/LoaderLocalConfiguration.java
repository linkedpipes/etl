package com.linkedpipes.plugin.loader.local;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = LoaderLocalVocabulary.CONFIG)
public class LoaderLocalConfiguration {

    @RdfToPojo.Property(iri = LoaderLocalVocabulary.HAS_PATH)
    private String path;

    public LoaderLocalConfiguration() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
