package com.linkedpipes.plugin.transformer.filesFilter;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = FilesFilterVocabulary.CONFIGURATION)
public class FilesFilterConfiguration {

    @RdfToPojo.Property(uri = FilesFilterVocabulary.HAS_PATTERN)
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
