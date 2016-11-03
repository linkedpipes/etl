package com.linkedpipes.plugin.transformer.templatedxlstocsv;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 */
@RdfToPojo.Type(uri = XlsToCsvVocabulary.CONFIGURATION)
public class XlsToCsvConfiguration {

    @RdfToPojo.Property(uri = XlsToCsvVocabulary.HAS_PREFIX)
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
