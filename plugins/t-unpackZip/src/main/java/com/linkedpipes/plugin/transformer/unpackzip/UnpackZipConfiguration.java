package com.linkedpipes.plugin.transformer.unpackzip;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = UnpackZipVocabulary.CONFIG_CLASS)
public class UnpackZipConfiguration {

    @RdfToPojo.Property(iri = UnpackZipVocabulary.CONFIG_USE_PREFIX)
    private boolean usePrefix = true;

    public UnpackZipConfiguration() {
    }

    public boolean isUsePrefix() {
        return usePrefix;
    }

    public void setUsePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
    }

}
