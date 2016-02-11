package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = XsltVocabulary.CONFIG_CLASS)
public class XsltConfiguration {

    @RdfToPojo.Property(uri = XsltVocabulary.TEMPLATE)
    private String xsltTemplate = "";

    @RdfToPojo.Property(uri = XsltVocabulary.EXTENSION)
    private String newExtension;

    public XsltConfiguration() {
    }

    public String getXsltTemplate() {
        return xsltTemplate;
    }

    public void setXsltTemplate(String xsltTemplate) {
        this.xsltTemplate = xsltTemplate;
    }

    public String getNewExtension() {
        return newExtension;
    }

    public void setNewExtension(String newExtension) {
        this.newExtension = newExtension;
    }
    
}
