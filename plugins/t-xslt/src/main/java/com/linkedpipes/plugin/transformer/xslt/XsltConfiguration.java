package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = XsltVocabulary.CONFIG_CLASS)
public class XsltConfiguration {

    @RdfToPojo.Property(iri = XsltVocabulary.TEMPLATE)
    private String xsltTemplate = "";

    @RdfToPojo.Property(iri = XsltVocabulary.EXTENSION)
    private String newExtension;

    @RdfToPojo.Property(iri = XsltVocabulary.SKIP_ON_ERROR)
    private boolean skipOnError = false;

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

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }
}
