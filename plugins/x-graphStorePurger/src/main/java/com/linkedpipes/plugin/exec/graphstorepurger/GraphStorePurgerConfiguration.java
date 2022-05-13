package com.linkedpipes.plugin.exec.graphstorepurger;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = GraphStorePurgerVocabulary.CONFIG_CLASS)
public class GraphStorePurgerConfiguration {

    public enum RepositoryType {
        VIRTUOSO,
        DEFAULT,
    }

    /**
     * Graph store endpoint.
     */
    @RdfToPojo.Property(iri = GraphStorePurgerVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(iri = GraphStorePurgerVocabulary.HAS_USERNAME)
    private String username;

    @RdfToPojo.Property(iri = GraphStorePurgerVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(iri = GraphStorePurgerVocabulary.HAS_AUTHENTICATION)
    private boolean useAuthentication = false;

    @RdfToPojo.Property(iri = GraphStorePurgerVocabulary.HAS_REPOSITORY)
    private RepositoryType repository = RepositoryType.VIRTUOSO;

    public GraphStorePurgerConfiguration() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public RepositoryType getRepository() {
        return repository;
    }

    public void setRepository(RepositoryType repository) {
        this.repository = repository;
    }

}
