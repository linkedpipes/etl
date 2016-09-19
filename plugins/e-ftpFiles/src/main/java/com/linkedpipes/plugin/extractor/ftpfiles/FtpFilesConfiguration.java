package com.linkedpipes.plugin.extractor.ftpfiles;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = FtpFilesVocabulary.CONFIG)
public class FtpFilesConfiguration {

    @RdfToPojo.Type(uri = FtpFilesVocabulary.REFERENCE)
    public static class Reference {

        @RdfToPojo.Property(uri = FtpFilesVocabulary.HAS_URI)
        private String uri;

        @RdfToPojo.Property(uri = FtpFilesVocabulary.HAS_NAME)
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

    @RdfToPojo.Property(uri = FtpFilesVocabulary.HAS_REFERENCE)
    private List<Reference> references = new LinkedList<>();

    @RdfToPojo.Property(uri = FtpFilesVocabulary.HAS_PASSIVE_MODE)
    private boolean usePassiveMode = false;

    @RdfToPojo.Property(uri = FtpFilesVocabulary.HAS_BINARY_MODE)
    private boolean useBinaryMode = false;

    @RdfToPojo.Property(uri = FtpFilesVocabulary.HAS_KEEP_ALIVE_CONTROL)
    private int keepAliveControl = 0;

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
