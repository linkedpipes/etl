package com.linkedpipes.plugin.loader.ftpfiles;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = FtpFilesLoaderVocabulary.CONFIG)
public class FtpFilesLoaderConfiguration {

    @RdfToPojo.Property(iri = FtpFilesLoaderVocabulary.HAS_USERNAME)
    private String user;

    @RdfToPojo.Property(iri = FtpFilesLoaderVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(iri = FtpFilesLoaderVocabulary.HAS_HOST)
    private String server;

    @RdfToPojo.Property(iri = FtpFilesLoaderVocabulary.HAS_PORT)
    private int port;

    @RdfToPojo.Property(iri = FtpFilesLoaderVocabulary.HAS_DIRECTORY)
    private String directory;

    @RdfToPojo.Property(iri = FtpFilesLoaderVocabulary.HAS_RETRY_COUNT)
    private int retryCount = 0;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

}
