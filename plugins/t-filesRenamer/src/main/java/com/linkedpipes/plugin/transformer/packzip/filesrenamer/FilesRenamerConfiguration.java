package com.linkedpipes.plugin.transformer.packzip.filesrenamer;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 */
@RdfToPojo.Type(uri = FilesRenamerVocabulary.CONFIGURATION)
public class FilesRenamerConfiguration {

    @RdfToPojo.Property(uri = FilesRenamerVocabulary.HAS_PATTERN)
    private String pattern;

    @RdfToPojo.Property(uri = FilesRenamerVocabulary.HAS_REPLACE_WITH)
    private String replaceWith;

    public FilesRenamerConfiguration() {
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getReplaceWith() {
        return replaceWith;
    }

    public void setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
    }

}
