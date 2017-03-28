package com.linkedpipes.plugin.loader.scp;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = LoaderScpVocabulary.CONFIG)
public class LoaderScpConfiguration {

    @RdfToPojo.Property(iri = LoaderScpVocabulary.HAS_USERNAME)
    private String userName;

    @RdfToPojo.Property(iri = LoaderScpVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(iri = LoaderScpVocabulary.HAS_HOST)
    private String host;

    @RdfToPojo.Property(iri = LoaderScpVocabulary.HAS_PORT)
    private int port;

    @RdfToPojo.Property(iri = LoaderScpVocabulary.HAS_TARGET_DIRECTORY)
    private String targetDirectory;

    @RdfToPojo.Property(iri = LoaderScpVocabulary.HAS_CREATE_DIRECTORY)
    private boolean createDirectory = false;

    @RdfToPojo.Property(iri = LoaderScpVocabulary.HAS_CLEAR_DIRECTORY)
    private boolean clearDirectory = false;

    public LoaderScpConfiguration() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public boolean isCreateDirectory() {
        return createDirectory;
    }

    public void setCreateDirectory(boolean createDirectory) {
        this.createDirectory = createDirectory;
    }

    public boolean isClearDirectory() {
        return clearDirectory;
    }

    public void setClearDirectory(boolean clearDirectory) {
        this.clearDirectory = clearDirectory;
    }
}
