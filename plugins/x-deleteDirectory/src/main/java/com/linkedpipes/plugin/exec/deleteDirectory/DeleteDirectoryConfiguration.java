package com.linkedpipes.plugin.exec.deleteDirectory;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 * @author Škoda Petr
 */
@RdfToPojo.Type(uri = DeleteDirectoryVocabulary.CONFIG_CLASS)
public class DeleteDirectoryConfiguration {

    @RdfToPojo.Property(uri = DeleteDirectoryVocabulary.HAS_DIRECTORY)
    private String directory = null;

    public DeleteDirectoryConfiguration() {
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
