package com.linkedpipes.plugin.exec.httprequest;

import com.linkedpipes.etl.executor.api.v1.component.task.TaskExecutionConfiguration;
import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = HttpRequestVocabulary.CONFIGURATION)
public class HttpRequestConfiguration implements TaskExecutionConfiguration {

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

    @Override
    public int getThreadsNumber() {
        return threadsNumber;
    }

    public void setThreadsNumber(int threads) {
        this.threadsNumber = threads;
    }

    @Override
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

}
