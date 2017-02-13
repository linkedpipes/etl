package com.linkedpipes.plugin.transformer.unpack;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = UnpackVocabulary.CONFIG_CLASS)
public class UnpackConfiguration {

    @RdfToPojo.Property(iri = UnpackVocabulary.HAS_USE_PREFIX)
    private boolean usePrefix = true;

    @RdfToPojo.Property(iri = UnpackVocabulary.HAS_FORMAT)
    private String format;

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

}
