package com.linkedpipes.plugin.transformer.rdfToHdt;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = RdfToHdtVocabulary.CONFIG)
public class RdfToHdtConfiguration {

    @RdfToPojo.Property(iri = RdfToHdtVocabulary.HAS_FILE_NAME)
    private String fileName;

    @RdfToPojo.Property(iri = RdfToHdtVocabulary.HAS_BASE_IRI)
    private String baseIri;

    public RdfToHdtConfiguration() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBaseIri() {
        return baseIri;
    }

    public void setBaseIri(String baseIri) {
        this.baseIri = baseIri;
    }

}
