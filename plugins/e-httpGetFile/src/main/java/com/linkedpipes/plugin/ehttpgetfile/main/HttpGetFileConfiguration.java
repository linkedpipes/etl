package com.linkedpipes.plugin.ehttpgetfile.main;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;
import com.linkedpipes.plugin.ehttpgetfile.Downloader;

@RdfToPojo.Type(iri = HttpGetFileVocabulary.CONFIG)
public class HttpGetFileConfiguration {

    @RdfToPojo.Property(iri = HttpGetFileVocabulary.HAS_URI)
    private String uri;

    @RdfToPojo.Property(iri = HttpGetFileVocabulary.HAS_NAME)
    private String fileName;

    /**
     * Force custom redirect. The Java follow only redirect in scope of
     * a protocol. So specially it does not allow redirect from http
     * to https - see
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4620571 .
     * <p>
     * If true DPU follow redirect to any location and protocol.
     */
    @RdfToPojo.Property(iri = HttpGetFileVocabulary.HAS_FOLLOW_REDIRECT)
    private boolean manualFollowRedirect;

    @RdfToPojo.Property(iri = HttpGetFileVocabulary.HAS_UTF8_REDIRECT)
    private boolean utf8Redirect;

    @RdfToPojo.Property(iri = HttpGetFileVocabulary.HAS_USER_AGENT)
    private String userAgent = null;

    @RdfToPojo.Property(iri = HttpGetFileVocabulary.ENCODE_URL)
    private boolean encodeUrl = false;

    public HttpGetFileConfiguration() {
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

    public boolean isManualFollowRedirect() {
        return manualFollowRedirect;
    }

    public void setManualFollowRedirect(boolean manualFollowRedirect) {
        this.manualFollowRedirect = manualFollowRedirect;
    }

    public boolean isUtf8Redirect() {
        return utf8Redirect;
    }

    public void setUtf8Redirect(boolean utf8Redirect) {
        this.utf8Redirect = utf8Redirect;
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

    public Downloader.Configuration asDownloaderConfiguration() {
        return new Downloader.Configuration(
                manualFollowRedirect, false, encodeUrl, utf8Redirect);
    }

}
