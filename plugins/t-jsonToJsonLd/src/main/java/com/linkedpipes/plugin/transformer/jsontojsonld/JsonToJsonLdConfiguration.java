package com.linkedpipes.plugin.transformer.jsontojsonld;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = JsonToJsonLdVocabulary.CONFIGURATION)
public class JsonToJsonLdConfiguration {

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_CONTEXT)
    private String context;

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_ENCODING)
    private String encoding;

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_FILE_REFERENCE)
    private boolean fileReference = false;

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_DATA_PREDICATE)
    private String dataPredicate;

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_TYPE)
    private String type;

    @RdfToPojo.Property(iri = JsonToJsonLdVocabulary.HAS_FILE_PREDICATE)
    private String filePredicate;

    public JsonToJsonLdConfiguration() {
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isFileReference() {
        return fileReference;
    }

    public void setFileReference(boolean fileReference) {
        this.fileReference = fileReference;
    }

    public String getDataPredicate() {
        return dataPredicate;
    }

    public void setDataPredicate(String dataPredicate) {
        this.dataPredicate = dataPredicate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilePredicate() {
        return filePredicate;
    }

    public void setFilePredicate(String filePredicate) {
        this.filePredicate = filePredicate;
    }
}
