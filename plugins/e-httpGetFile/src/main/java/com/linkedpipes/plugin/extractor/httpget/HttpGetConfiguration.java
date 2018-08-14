package com.linkedpipes.plugin.extractor.httpget;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = HttpGetVocabulary.CONFIG)
public class HttpGetConfiguration {

    @RdfToPojo.Property(iri = HttpGetVocabulary.HAS_URI)
    private String uri;

    @RdfToPojo.Property(iri = HttpGetVocabulary.HAS_NAME)
    private String fileName;

    /**
     * Force custom redirect. The Java follow only redirect in scope of
     * a protocol. So specially it does not allow redirect from http
     * to https - see
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4620571 .
     *
     * If true DPU follow redirect to any location and protocol.
     */
    @RdfToPojo.Property(iri = HttpGetVocabulary.HAS_FOLLOW_REDIRECT)
    private boolean forceFollowRedirect;

    @RdfToPojo.Property(iri = HttpGetVocabulary.HAS_USER_AGENT)
    private String userAgent = null;

    @RdfToPojo.Property(iri = HttpGetVocabulary.ENCODE_URL)
    private boolean encodeUrl = false;

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

    public boolean isForceFollowRedirect() {
        return forceFollowRedirect;
    }

    public void setForceFollowRedirect(boolean forceFollowRedirect) {
        this.forceFollowRedirect = forceFollowRedirect;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isEncodeUrl() {
        return encodeUrl;
    }

    public void setEncodeUrl(boolean encodeUrl) {
        this.encodeUrl = encodeUrl;
    }

}
