package com.linkedpipes.etl.plugin.transformer.filedecode;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FileDecodeVocabulary.CONFIG)
public class FileDecodeConfiguration {

    @RdfToPojo.Property(iri = FileDecodeVocabulary.HAS_SKIP_ERROR)
    private boolean skipOnError = false;

    public FileDecodeConfiguration() {
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

}
