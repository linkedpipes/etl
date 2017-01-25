package com.linkedpipes.plugin.transformer.streamcompression;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

@RdfToPojo.Type(uri = StreamCompressionVocabulary.CONFIG_CLASS)
public class StreamCompressionConfiguration {

    @RdfToPojo.Property(uri = StreamCompressionVocabulary.HAS_FORMAT)
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
