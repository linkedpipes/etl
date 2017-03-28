package com.linkedpipes.plugin.transformer.packzip;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = PackZipVocabulary.CONFIG)
public class PackZipConfiguration {

    @RdfToPojo.Property(iri = PackZipVocabulary.HAS_FILE_NAME)
    private String fileName;

    public PackZipConfiguration() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
