package com.linkedpipes.plugin.transformer.templatedxlstocsv;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = XlsToCsvVocabulary.CONFIGURATION)
public class XlsToCsvConfiguration {

    @RdfToPojo.Property(iri = XlsToCsvVocabulary.HAS_PREFIX)
    private String template_prefix = "SABLONA_";

    public XlsToCsvConfiguration() {
    }

    public String getTemplate_prefix() {
        return template_prefix;
    }

    public void setTemplate_prefix(String template_prefix) {
        this.template_prefix = template_prefix;
    }

}
