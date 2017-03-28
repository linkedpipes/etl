package com.linkedpipes.plugin.transformer.filesFilter;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FilesFilterVocabulary.CONFIGURATION)
public class FilesFilterConfiguration {

    @RdfToPojo.Property(iri = FilesFilterVocabulary.HAS_PATTERN)
    private String fileNamePattern = ".*";

    public FilesFilterConfiguration() {
    }

    public String getFileNamePattern() {
        return fileNamePattern;
    }

    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }

}
