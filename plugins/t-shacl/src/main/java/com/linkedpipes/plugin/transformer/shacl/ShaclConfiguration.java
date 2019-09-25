package com.linkedpipes.plugin.transformer.shacl;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = ShaclVocabulary.CONFIG)
public class ShaclConfiguration {

    @RdfToPojo.Property(iri = ShaclVocabulary.HAS_SHAPE)
    private String shapes = "";

    @RdfToPojo.Property(iri = ShaclVocabulary.HAS_FAIL_ON_ERROR)
    private boolean failOnError = true;

    public ShaclConfiguration() {
    }

    public String getShapes() {
        return shapes;
    }

    public void setShapes(String shapes) {
        this.shapes = shapes;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

}
