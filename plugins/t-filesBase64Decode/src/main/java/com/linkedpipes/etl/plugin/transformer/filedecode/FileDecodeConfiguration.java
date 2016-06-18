package com.linkedpipes.etl.plugin.transformer.filedecode;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = FileDecodeVocabulary.CONFIG)
public class FileDecodeConfiguration {

    @RdfToPojo.Property(uri = FileDecodeVocabulary.HAS_SKIP_ERROR)
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
