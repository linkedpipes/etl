package com.linkedpipes.plugin.transformer.packzip;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = PackZipVocabulary.CONFIG_CLASS)
public class PackZipConfiguration {

    @RdfToPojo.Property(uri = PackZipVocabulary.CONFIG_FILE_NAME)
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
