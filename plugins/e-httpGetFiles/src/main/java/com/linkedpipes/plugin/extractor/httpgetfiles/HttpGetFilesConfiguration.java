package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = HttpGetFilesVocabulary.CONFIG)
public class HttpGetFilesConfiguration implements TaskExecutionConfiguration {

    /**
     * Force custom redirect. The Java follow only redirect in scope of
     * a protocol. So specially it does not allow redirect from http
     * to https - see
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4620571 .
     *
     * If true DPU follow redirect to any location and protocol.
     */
    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_FOLLOW_REDIRECT)
    private boolean manualFollowRedirect = true;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_UTF8_REDIRECT)
    private boolean utf8Redirect;

    /**
     * If true skip file in case of an error.
     */
    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.SKIP_ON_ERROR)
    private boolean skipOnError = false;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_HEADER)
    private List<RequestHeader> headers = new LinkedList<>();

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_THREADS)
    private int threads = 1;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_DETAIL_LOG)
    private boolean detailLogging = false;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_TIMEOUT)
    private Integer timeout;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_THREADS_PER_GROUP)
    private int threadsPerGroup = 1;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.ENCODE_URL)
    private boolean encodeUrl = false;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_RETRY_COUNT)
    private int retryCount = 1;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_WAIT_TIME)
    private int waitTime = 0;

    public HttpGetFilesConfiguration() {
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

    @Override
    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    public List<RequestHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<RequestHeader> headers) {
        this.headers = headers;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public boolean isDetailLogging() {
        return detailLogging;
    }

    public void setDetailLogging(boolean detailLogging) {
        this.detailLogging = detailLogging;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public int getThreadsNumber() {
        return threads;
    }

    public int getThreadsPerGroup() {
        return threadsPerGroup;
    }

    public void setThreadsPerGroup(int threadsPerGroup) {
        this.threadsPerGroup = threadsPerGroup;
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

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

}
