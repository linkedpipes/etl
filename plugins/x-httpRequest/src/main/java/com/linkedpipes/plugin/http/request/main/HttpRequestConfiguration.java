package com.linkedpipes.plugin.http.request.main;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = HttpRequestVocabulary.CONFIGURATION)
public class HttpRequestConfiguration {

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_THREADS)
    private int threadsNumber = 1;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_SKIP_ON_ERROR)
    private boolean skipOnError = false;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_THREADS_PER_GROUP)
    private int threadsPerGroup = 1;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_FOLLOW_REDIRECT)
    private boolean followRedirect = false;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.ENCODE_URL)
    private boolean encodeUrl = false;

    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_TIME_OUT)
    private Integer timeOut = null;

    // Not used anymore.
    @RdfToPojo.Property(iri = HttpRequestVocabulary.HAS_UTF8_REDIRECT)
    private boolean hasUtf8Redirect = false;

    public int getThreadsNumber() {
        return threadsNumber;
    }

    public void setThreadsNumber(int threads) {
        this.threadsNumber = threads;
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    public int getThreadsPerGroup() {
        return threadsPerGroup;
    }

    public void setThreadsPerGroup(int threadsPerGroup) {
        this.threadsPerGroup = threadsPerGroup;
    }

    public boolean isFollowRedirect() {
        return followRedirect;
    }

    public void setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;
    }

    public boolean isEncodeUrl() {
        return encodeUrl;
    }

    public void setEncodeUrl(boolean encodeUrl) {
        this.encodeUrl = encodeUrl;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isHasUtf8Redirect() {
        return hasUtf8Redirect;
    }

    public void setHasUtf8Redirect(boolean hasUtf8Redirect) {
        this.hasUtf8Redirect = hasUtf8Redirect;
    }

}
