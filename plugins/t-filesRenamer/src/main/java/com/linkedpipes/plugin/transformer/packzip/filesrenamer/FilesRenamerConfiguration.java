package com.linkedpipes.plugin.transformer.packzip.filesrenamer;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FilesRenamerVocabulary.CONFIGURATION)
public class FilesRenamerConfiguration {

    @RdfToPojo.Property(iri = FilesRenamerVocabulary.HAS_PATTERN)
    private String pattern;

    @RdfToPojo.Property(iri = FilesRenamerVocabulary.HAS_REPLACE_WITH)
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
