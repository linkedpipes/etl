package com.linkedpipes.plugin.extractor.ftpfiles;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(iri = FtpFilesVocabulary.CONFIG)
public class FtpFilesConfiguration {

    @RdfToPojo.Type(iri = FtpFilesVocabulary.REFERENCE)
    public static class Reference {

        @RdfToPojo.Property(iri = FtpFilesVocabulary.HAS_URI)
        private String uri;

        @RdfToPojo.Property(iri = FtpFilesVocabulary.HAS_NAME)
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

    @RdfToPojo.Property(iri = FtpFilesVocabulary.HAS_REFERENCE)
    private List<Reference> references = new LinkedList<>();

    @RdfToPojo.Property(iri = FtpFilesVocabulary.HAS_PASSIVE_MODE)
    private boolean usePassiveMode = false;

    @RdfToPojo.Property(iri = FtpFilesVocabulary.HAS_BINARY_MODE)
    private boolean useBinaryMode = true;

    @RdfToPojo.Property(iri = FtpFilesVocabulary.HAS_KEEP_ALIVE_CONTROL)
    private int keepAliveControl = 5000;

    public FtpFilesConfiguration() {
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(
            List<Reference> references) {
        this.references = references;
    }

    public boolean isUsePassiveMode() {
        return usePassiveMode;
    }

    public void setUsePassiveMode(boolean usePassiveMode) {
        this.usePassiveMode = usePassiveMode;
    }

    public boolean isUseBinaryMode() {
        return useBinaryMode;
    }

    public void setUseBinaryMode(boolean useBinaryMode) {
        this.useBinaryMode = useBinaryMode;
    }

    public int getKeepAliveControl() {
        return keepAliveControl;
    }

    public void setKeepAliveControl(int keepAliveControl) {
        this.keepAliveControl = keepAliveControl;
    }

}
