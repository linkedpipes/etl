package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.executor.api.v1.component.task.Task;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = HttpRequestVocabulary.TASK)
public class HttpRequestTask implements Task {

    @RdfToPojo.Type(iri = HttpRequestVocabulary.HEADER)
    public static class Header {

        @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_NAME)
        private String name;

        @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_VALUE)
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @RdfToPojo.Type(iri = HttpRequestVocabulary.CONTENT)
    public static class Content {

        @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_NAME)
        private String name;

        @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_VALUE)
        private String value;

        @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_FILE_NAME)
        private String fileName;

        @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_FILE_REFERENCE)
        private String fileReference;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileReference() {
            return fileReference;
        }

        public void setFileReference(String fileReference) {
            this.fileReference = fileReference;
        }

    }

    @RdfToPojo.Resource
    private String iri;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_URL)
    private String url;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_METHOD)
    private String method;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_POST_CONTENT_AS_BODY)
    private boolean postContentAsBody = false;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_HEADER)
    private List<Header> headers = new LinkedList<>();

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_CONTENT)
    private List<Content> content = new LinkedList<>();

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_FILE_NAME)
    private String outputFileName;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_OUTPUT_HEADERS)
    private boolean outputHeaders = false;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_GROUP)
    private String group = null;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_FOLLOW_REDIRECT)
    private Boolean followRedirect = null;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_TIME_OUT)
    private Integer timeOut = null;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_UTF8_REDIRECT)
    private Boolean hasUtf8Redirect = null;

    @Override
    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isPostContentAsBody() {
        return postContentAsBody;
    }

    public void setPostContentAsBody(boolean postContentAsBody) {
        this.postContentAsBody = postContentAsBody;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(
            List<Header> headers) {
        this.headers = headers;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public boolean isOutputHeaders() {
        return outputHeaders;
    }

    public void setOutputHeaders(boolean outputHeaders) {
        this.outputHeaders = outputHeaders;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Boolean isFollowRedirect() {
        return followRedirect;
    }

    public void setFollowRedirect(Boolean followRedirect) {
        this.followRedirect = followRedirect;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public Boolean isHasUtf8Redirect() {
        return hasUtf8Redirect;
    }

    public void setHasUtf8Redirect(Boolean hasUtf8Redirect) {
        this.hasUtf8Redirect = hasUtf8Redirect;
    }

}
