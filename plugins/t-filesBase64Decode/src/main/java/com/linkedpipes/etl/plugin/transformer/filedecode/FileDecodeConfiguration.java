package com.linkedpipes.etl.plugin.transformer.filedecode;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = "http://plugins.linkedpipes.com/ontology/t-fileDecode#Configuration")
public class FileDecodeConfiguration {

    @RdfToPojo.Property(uri = "http://plugins.linkedpipes.com/ontology/t-fileDecode#skipOnError")
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
