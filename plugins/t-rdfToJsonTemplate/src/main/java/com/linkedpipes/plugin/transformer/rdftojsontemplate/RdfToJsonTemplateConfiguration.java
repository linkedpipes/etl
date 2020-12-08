package com.linkedpipes.plugin.transformer.rdftojsontemplate;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = RdfToJsonTemplateVocabulary.CONFIGURATION)
public class RdfToJsonTemplateConfiguration {

    @RdfToPojo.Property(iri = RdfToJsonTemplateVocabulary.MAPPING)
    private String mapping = "";

    public RdfToJsonTemplateConfiguration() {
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

}
