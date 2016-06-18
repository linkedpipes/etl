package com.linkedpipes.plugin.loader.local;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = LoaderLocalVocabulary.CONFIG)
public class LoaderLocalConfiguration {

    @RdfToPojo.Property(uri = LoaderLocalVocabulary.HAS_PATH)
    private String path;

    public LoaderLocalConfiguration() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
