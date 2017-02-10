package com.linkedpipes.plugin.transformer.textHolder;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = TextHolderVocabulary.CONFIG)
public class TextHolderConfiguration {

    @RdfToPojo.Property(iri = TextHolderVocabulary.HAS_FILE_NAME)
    private String fileName = "file.txt";

    @RdfToPojo.Property(iri = TextHolderVocabulary.HAS_CONTENT)
    private String content = "";

    public TextHolderConfiguration() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
