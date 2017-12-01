package com.linkedpipes.plugin.transformer.unpack;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = UnpackVocabulary.CONFIG_CLASS)
public class UnpackConfiguration {

    @RdfToPojo.Property(iri = UnpackVocabulary.HAS_USE_PREFIX)
    private boolean usePrefix = true;

    @RdfToPojo.Property(iri = UnpackVocabulary.HAS_FORMAT)
    private String format;

    @RdfToPojo.Property(iri = UnpackVocabulary.SKIP_ON_ERROR)
    private boolean skipOnError = false;

    public UnpackConfiguration() {
    }

    public boolean isUsePrefix() {
        return usePrefix;
    }

    public void setUsePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }
}
