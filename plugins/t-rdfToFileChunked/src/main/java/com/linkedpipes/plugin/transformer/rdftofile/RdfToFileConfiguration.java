package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = RdfToFileVocabulary.CONFIG)
public class RdfToFileConfiguration {

    @RdfToPojo.Property(uri = RdfToFileVocabulary.HAS_FILE_NAME)
    private String fileName;

    @RdfToPojo.Property(uri = RdfToFileVocabulary.HAS_FILE_TYPE)
    private String fileType;

    @RdfToPojo.Property(uri = RdfToFileVocabulary.HAS_GRAPH_URI)
    private String graphUri;

    @RdfToPojo.Property(uri = RdfToFileVocabulary.HAS_PREFIX_TEST)
    private String prefixes;

    public RdfToFileConfiguration() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getGraphUri() {
        return graphUri;
    }

    public void setGraphUri(String graphUri) {
        this.graphUri = graphUri;
    }

    public String getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(String prefixes) {
        this.prefixes = prefixes;
    }
}
