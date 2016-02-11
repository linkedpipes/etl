package com.linkedpipes.plugin.extractor.httpget;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = HttpGetVocabulary.CONFIG_CLASS)
public class HttpGetConfiguration {

    @RdfToPojo.Property(uri = HttpGetVocabulary.CONFIG_ENTRY_URI)
    private String uri;

    @RdfToPojo.Property(uri = HttpGetVocabulary.CONFIG_ENTRY_NAME)
    private String fileName;

    public HttpGetConfiguration() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
