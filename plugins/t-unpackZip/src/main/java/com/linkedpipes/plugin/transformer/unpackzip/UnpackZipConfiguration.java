package com.linkedpipes.plugin.transformer.unpackzip;

import com.linkedpipes.etl.dpu.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = UnpackZipVocabulary.CONFIG_CLASS)
public class UnpackZipConfiguration {

    @RdfToPojo.Property(uri = UnpackZipVocabulary.CONFIG_USE_PREFIX)
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
