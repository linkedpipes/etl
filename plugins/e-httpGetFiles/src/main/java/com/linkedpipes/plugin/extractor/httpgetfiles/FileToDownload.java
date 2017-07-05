package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = HttpGetFilesVocabulary.REFERENCE)
public class FileToDownload {

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_URI)
    private String uri;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_NAME)
    private String fileName;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_HEADER)
    private List<RequestHeader> headers = new LinkedList<>();

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_TMEOUT)
    private Integer timeOut = null;

    public FileToDownload() {
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

    public List<RequestHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<RequestHeader> headers) {
        this.headers = headers;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

}
