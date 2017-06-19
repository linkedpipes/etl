package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = HttpGetFilesVocabulary.HEADER)
public class RequestHeader {

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_KEY)
    private String key;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_VALUE)
    private String value;

    public RequestHeader() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
