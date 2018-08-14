package com.linkedpipes.plugin.loader.solr;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SolrLoaderVocabulary.CONFIG_CLASS)
public class SolrLoaderConfiguration {

    @RdfToPojo.Property(iri = SolrLoaderVocabulary.HAS_SERVER)
    private String server;

    @RdfToPojo.Property(iri = SolrLoaderVocabulary.HAS_CORE)
    private String core;

    @RdfToPojo.Property(iri = SolrLoaderVocabulary.HAS_REPLACE)
    private boolean deleteBeforeLoading = false;

    @RdfToPojo.Property(iri = SolrLoaderVocabulary.HAS_USE_AUTHENTICATION)
    private boolean useAuthentication = false;

    @RdfToPojo.Property(iri = SolrLoaderVocabulary.HAS_USER_NAME)
    private String userName;

    @RdfToPojo.Property(iri = SolrLoaderVocabulary.HAS_PASSWORD)
    private String password;

    public SolrLoaderConfiguration() {
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getCore() {
        return core;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public boolean isDeleteBeforeLoading() {
        return deleteBeforeLoading;
    }

    public void setDeleteBeforeLoading(boolean deleteBeforeLoading) {
        this.deleteBeforeLoading = deleteBeforeLoading;
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
