package com.linkedpipes.plugin.transformer.chunkedtofiles;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = ChunkedToFilesVocabulary.CONFIG)
public class ChunkedToFilesConfiguration {

    @RdfToPojo.Property(iri = ChunkedToFilesVocabulary.HAS_FILE_TYPE)
    private String fileType = null;

    @RdfToPojo.Property(iri = ChunkedToFilesVocabulary.HAS_GRAPH_URI)
    private String graphUri = null;

    @RdfToPojo.Property(iri = ChunkedToFilesVocabulary.HAS_PREFIX_TTL)
    private String prefixTurtle = "";

    public ChunkedToFilesConfiguration() {
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

    public String getPrefixTurtle() {
        return prefixTurtle;
    }

    public void setPrefixTurtle(String prefixTurtle) {
        this.prefixTurtle = prefixTurtle;
    }

}