package com.linkedpipes.plugin.exec.deleteDirectory;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = DeleteDirectoryVocabulary.CONFIG_CLASS)
public class DeleteDirectoryConfiguration {

    @RdfToPojo.Property(iri = DeleteDirectoryVocabulary.HAS_DIRECTORY)
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
