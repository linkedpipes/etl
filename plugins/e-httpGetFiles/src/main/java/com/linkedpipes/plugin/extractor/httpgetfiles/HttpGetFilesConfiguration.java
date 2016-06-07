package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.dpu.api.service.RdfToPojo;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = HttpGetFilesVocabulary.CONFIG_CLASS)
public class HttpGetFilesConfiguration {

    @RdfToPojo.Type(uri = HttpGetFilesVocabulary.REFERENCE_CLASS)
    public static class Reference {

        @RdfToPojo.Property(uri = HttpGetFilesVocabulary.REFERENCE_HAS_URI)
        private String uri;

        @RdfToPojo.Property(uri = HttpGetFilesVocabulary.REFERENCE_HAS_NAME)
        private String fileName;

        public Reference() {
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

    }

    @RdfToPojo.Property(uri = HttpGetFilesVocabulary.CONFIG_HAS_REFERENCE)
    private List<Reference> references = new LinkedList<>();

    /**
     * Force custom redirect. The Java follow only redirect in scope of
     * a protocol. So specially it does not allow redirect from http
     * to https - see
     * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4620571 .
     *
     * If true DPU follow redirect to any location and protocol.
     */
    @RdfToPojo.Property(uri = HttpGetFilesVocabulary.CONFIG_HAS_FOLLOW_REDIRECT)
    private boolean forceFollowRedirect = true;

    /**
     * If true skip file in case of an error.
     */
    @RdfToPojo.Property(uri = HttpGetFilesVocabulary.SKIP_ON_ERROR)
    private boolean skipOnError = false;

    public HttpGetFilesConfiguration() {
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
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

}
