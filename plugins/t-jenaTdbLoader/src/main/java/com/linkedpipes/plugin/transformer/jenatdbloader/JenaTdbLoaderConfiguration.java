package com.linkedpipes.plugin.transformer.jenatdbloader;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JenaTdbLoaderVocabulary.CONFIG)
public class JenaTdbLoaderConfiguration {

    @RdfToPojo.Property(iri = JenaTdbLoaderVocabulary.HAS_LOCATION)
    private String location;

    @RdfToPojo.Property(iri = JenaTdbLoaderVocabulary.HAS_LOADER)
    private String loader;

    public JenaTdbLoaderConfiguration() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLoader() {
        return loader;
    }

    public void setLoader(String loader) {
        this.loader = loader;
    }

}
