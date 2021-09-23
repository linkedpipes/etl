package com.linkedpipes.plugin.loader.local;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = LoaderLocalVocabulary.CONFIG)
public class LoaderLocalConfiguration {

    @RdfToPojo.Property(iri = LoaderLocalVocabulary.HAS_PATH)
    private String path;

    @RdfToPojo.Property(iri = LoaderLocalVocabulary.HAS_FILE_PERMISSIONS)
    private String filePermissions = null;

    @RdfToPojo.Property(iri = LoaderLocalVocabulary.HAS_DIRECTORY_PERMISSIONS)
    private String directoryPermissions = null;

    public LoaderLocalConfiguration() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilePermissions() {
        return filePermissions;
    }

    public void setFilePermissions(String filePermissions) {
        this.filePermissions = filePermissions;
    }

    public String getDirectoryPermissions() {
        return directoryPermissions;
    }

    public void setDirectoryPermissions(String directoryPermissions) {
        this.directoryPermissions = directoryPermissions;
    }

}
