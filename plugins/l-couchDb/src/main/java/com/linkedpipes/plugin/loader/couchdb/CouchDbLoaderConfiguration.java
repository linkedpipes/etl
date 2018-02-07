package com.linkedpipes.plugin.loader.couchdb;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = CouchDbLoaderVocabulary.CONFIGURATION)
public class CouchDbLoaderConfiguration {

    @RdfToPojo.Property(iri = CouchDbLoaderVocabulary.HAS_URL)
    private String url;

    @RdfToPojo.Property(iri = CouchDbLoaderVocabulary.HAS_DATABASE)
    private String database;

    @RdfToPojo.Property(iri = CouchDbLoaderVocabulary.HAS_RECREATE_DATABASE)
    private boolean recreateDatabase;

    /**
     * Batch size in MB.
     */
    @RdfToPojo.Property(iri = CouchDbLoaderVocabulary.HAS_BATCH_SIZE)
    private int batchSize = 8;

    @RdfToPojo.Property(iri = CouchDbLoaderVocabulary.HAS_USE_AUTHENTICATION)
    private boolean useAuthentication = false;

    @RdfToPojo.Property(iri = CouchDbLoaderVocabulary.HAS_USER_NAME)
    private String userName;

    @RdfToPojo.Property(iri = CouchDbLoaderVocabulary.HAS_PASSWORD)
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public boolean isRecreateDatabase() {
        return recreateDatabase;
    }

    public void setRecreateDatabase(boolean recreateDatabase) {
        this.recreateDatabase = recreateDatabase;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
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

}
