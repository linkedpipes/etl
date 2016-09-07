package com.linkedpipes.plugin.extractor.httpget;

import com.linkedpipes.etl.component.api.Configuration;
import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = HttpGetVocabulary.CONFIG)
public class HttpGetConfiguration implements Configuration {

    @RdfToPojo.Property(uri = HttpGetVocabulary.HAS_URI)
    private String uri;

    @RdfToPojo.Property(uri = HttpGetVocabulary.HAS_NAME)
    private String fileName;

    /**
     * Force custom redirect. The Java follow only redirect in scope of
     * a protocol. So specially it does not allow redirect from http
     * to https - see
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4620571 .
     *
     * If true DPU follow redirect to any location and protocol.
     */
    @RdfToPojo.Property(uri = HttpGetVocabulary.HAS_FOLLOW_REDIRECT)
    private boolean forceFollowRedirect;

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

}
