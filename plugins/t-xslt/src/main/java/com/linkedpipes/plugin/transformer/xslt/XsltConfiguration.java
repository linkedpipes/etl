package com.linkedpipes.plugin.transformer.xslt;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = XsltVocabulary.CONFIG_CLASS)
public class XsltConfiguration {

    @RdfToPojo.Property(uri = XsltVocabulary.TEMPLATE)
    private String xsltTemplate = "";

    @RdfToPojo.Property(uri = XsltVocabulary.EXTENSION)
    private String newExtension;

    @RdfToPojo.Property(uri = XsltVocabulary.SKIP_ON_ERROR)
    private boolean skipOnError = false;

    @RdfToPojo.Property(uri = XsltVocabulary.THREADS)
    private int threads = 1;

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

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }
}
