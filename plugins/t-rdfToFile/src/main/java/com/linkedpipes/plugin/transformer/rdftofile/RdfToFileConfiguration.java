package com.linkedpipes.plugin.transformer.rdftofile;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = RdfToFileVocabulary.CONFIG)
public class RdfToFileConfiguration {

    @RdfToPojo.Property(iri = RdfToFileVocabulary.HAS_FILE_NAME)
    private String fileName;

    @RdfToPojo.Property(iri = RdfToFileVocabulary.HAS_FILE_TYPE)
    private String fileType;

    @RdfToPojo.Property(iri = RdfToFileVocabulary.HAS_GRAPH_URI)
    private String graphUri;

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

}
