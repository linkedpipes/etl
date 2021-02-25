package com.linkedpipes.plugin.transformer.jsonldformattitanium;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonLdFormatTitaniumVocabulary.CONFIGURATION)
public class JsonLdFormatTitaniumConfiguration {

    @RdfToPojo.Property(iri = JsonLdFormatTitaniumVocabulary.HAS_FORMAT)
    private String format;

    @RdfToPojo.Property(iri = JsonLdFormatTitaniumVocabulary.HAS_CONTEXT)
    private String context;

    @RdfToPojo.Property(iri = JsonLdFormatTitaniumVocabulary.HAS_FRAME)
    private String frame;

    public JsonLdFormatTitaniumConfiguration() {
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
