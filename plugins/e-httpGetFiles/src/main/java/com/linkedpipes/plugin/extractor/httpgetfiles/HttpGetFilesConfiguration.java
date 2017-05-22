package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = HttpGetFilesVocabulary.CONFIG)
public class HttpGetFilesConfiguration {

    @RdfToPojo.Type(iri = HttpGetFilesVocabulary.HEADER)
    public static class Header {

        @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_KEY)
        private String key;

        @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_VALUE)
        private String value;

        public Header() {
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

    /**
     * Force custom redirect. The Java follow only redirect in scope of
     * a protocol. So specially it does not allow redirect from http
     * to https - see
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4620571 .
     *
     * If true DPU follow redirect to any location and protocol.
     */
    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_FOLLOW_REDIRECT)
    private boolean forceFollowRedirect = true;

    /**
     * If true skip file in case of an error.
     */
    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.SKIP_ON_ERROR)
    private boolean skipOnError = false;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_HEADER)
    private List<Header> headers = new LinkedList<>();

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_THREADS)
    private int threads = 1;

    @RdfToPojo.Property(iri = HttpGetFilesVocabulary.HAS_DETAIL_LOG)
    private boolean detailLogging = false;

    public HttpGetFilesConfiguration() {
    }

    public boolean isForceFollowRedirect() {
        return forceFollowRedirect;
    }

    public void setForceFollowRedirect(boolean forceFollowRedirect) {
        this.forceFollowRedirect = forceFollowRedirect;
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
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
}
