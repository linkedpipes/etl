package com.linkedpipes.plugin.transformer.jsonldformat;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonLdFormatVocabulary.CONFIGURATION)
public class JsonLdFormatConfiguration {

    @RdfToPojo.Property(iri = JsonLdFormatVocabulary.HAS_FORMAT)
    private String format;

    @RdfToPojo.Property(iri = JsonLdFormatVocabulary.HAS_CONTEXT)
    private String context;

    @RdfToPojo.Property(iri = JsonLdFormatVocabulary.HAS_FRAME)
    private String frame;

    public JsonLdFormatConfiguration() {
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

}
