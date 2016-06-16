package com.linkedpipes.plugin.loader.local;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = LoaderLocalVocabulary.CONFIG_CLASS)
public class LoaderLocalConfiguration {

    @RdfToPojo.Property(uri = LoaderLocalVocabulary.HAS_PATH)
    public String path;

    public LoaderLocalConfiguration() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
