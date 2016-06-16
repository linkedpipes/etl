package com.linkedpipes.plugin.loader.scp;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = LoaderScpVocabulary.CONFIG_CLASS)
public class LoaderScpConfiguration {

    @RdfToPojo.Property(uri = LoaderScpVocabulary.USERNAME)
    private String userName;

    @RdfToPojo.Property(uri = LoaderScpVocabulary.PASSWORD)
    private String password;

    @RdfToPojo.Property(uri = LoaderScpVocabulary.HOST)
    private String host;

    @RdfToPojo.Property(uri = LoaderScpVocabulary.PORT)
    private int port;

    @RdfToPojo.Property(uri = LoaderScpVocabulary.TARGET_DIRECTORY)
    private String targetDirectory;

    @RdfToPojo.Property(uri = LoaderScpVocabulary.CREATE_DIRECTORY)
    private boolean createDirectory = false;

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

}
