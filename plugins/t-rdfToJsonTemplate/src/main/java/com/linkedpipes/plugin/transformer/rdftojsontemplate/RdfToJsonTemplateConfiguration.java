package com.linkedpipes.plugin.transformer.rdftojsontemplate;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = RdfToJsonTemplateVocabulary.CONFIGURATION)
public class RdfToJsonTemplateConfiguration {

    @RdfToPojo.Property(iri = RdfToJsonTemplateVocabulary.MAPPING)
    private String mapping = "";

    @RdfToPojo.Property(iri = RdfToJsonTemplateVocabulary.MULTIPLE_PRIMITIVES)
    private boolean ignoreMultiplePrimitives = false;

    public RdfToJsonTemplateConfiguration() {
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public boolean isIgnoreMultiplePrimitives() {
        return ignoreMultiplePrimitives;
    }

    public void setIgnoreMultiplePrimitives(boolean ignoreMultiplePrimitives) {
        this.ignoreMultiplePrimitives = ignoreMultiplePrimitives;
    }

}
