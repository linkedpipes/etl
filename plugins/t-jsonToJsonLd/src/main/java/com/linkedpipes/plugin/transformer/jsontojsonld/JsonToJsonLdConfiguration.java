package com.linkedpipes.plugin.transformer.jsontojsonld;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonToJsonLdVocabulary.CONFIGURATION)
public class JsonToJsonLdConfiguration {

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_VOCABULARY)
    private String vocabulary;

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_ENCODING)
    private String encoding;

    public JsonToJsonLdConfiguration() {
    }

    public String getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
