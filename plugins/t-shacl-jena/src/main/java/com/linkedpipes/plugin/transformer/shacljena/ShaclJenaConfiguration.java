package com.linkedpipes.plugin.transformer.shacljena;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = ShaclJenaVocabulary.CONFIG)
public class ShaclJenaConfiguration {

    @RdfToPojo.Property(iri = ShaclJenaVocabulary.HAS_FAIL_ON_ERROR)
    private boolean failOnError = true;

    @RdfToPojo.Property(iri = ShaclJenaVocabulary.HAS_OUTPUT_SHAPES)
    private boolean outputShapes = true;


    public ShaclJenaConfiguration() {
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isOutputShapes() {
        return outputShapes;
    }

    public void setOutputShapes(boolean outputShapes) {
        this.outputShapes = outputShapes;
    }

}
