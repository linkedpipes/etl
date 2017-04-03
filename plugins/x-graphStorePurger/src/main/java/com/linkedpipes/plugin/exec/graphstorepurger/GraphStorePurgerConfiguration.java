package com.linkedpipes.plugin.exec.graphstorepurger;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = GraphStorePurgerVocabulary.CONFIG_CLASS)
public class GraphStorePurgerConfiguration {

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
}
