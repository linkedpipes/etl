package com.linkedpipes.plugin.transformer.streamcompression;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = StreamCompressionVocabulary.CONFIG_CLASS)
public class StreamCompressionConfiguration {

    @RdfToPojo.Property(iri = StreamCompressionVocabulary.HAS_FORMAT)
    private String format;

    public StreamCompressionConfiguration() {
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
