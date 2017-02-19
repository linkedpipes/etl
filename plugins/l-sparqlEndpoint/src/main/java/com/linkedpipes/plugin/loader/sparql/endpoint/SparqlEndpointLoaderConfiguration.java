package com.linkedpipes.plugin.loader.sparql.endpoint;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlEndpointLoaderVocabulary.CONFIG)
public class SparqlEndpointLoaderConfiguration {

    @RdfToPojo.Property(iri = SparqlEndpointLoaderVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(iri = SparqlEndpointLoaderVocabulary.HAS_AUTH)
    private boolean useAuthentication = true;

    @RdfToPojo.Property(iri = SparqlEndpointLoaderVocabulary.HAS_USERNAME)
    private String userName;

    @RdfToPojo.Property(iri = SparqlEndpointLoaderVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(iri = SparqlEndpointLoaderVocabulary.HAS_CLEAR_GRAPH)
    private boolean clearDestinationGraph = false;

    @RdfToPojo.Property(iri = SparqlEndpointLoaderVocabulary.HAS_TAGET_GRAPH)
    private String targetGraphName;

    @RdfToPojo.Property(iri = SparqlEndpointLoaderVocabulary.HAS_COMMIT_SIZE)
    private int commitSize = 100000;

    public SparqlEndpointLoaderConfiguration() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public boolean isClearDestinationGraph() {
        return clearDestinationGraph;
    }

    public void setClearDestinationGraph(boolean clearDestinationGraph) {
        this.clearDestinationGraph = clearDestinationGraph;
    }

    public String getTargetGraphName() {
        return targetGraphName;
    }

    public void setTargetGraphName(String targetGraphName) {
        this.targetGraphName = targetGraphName;
    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }
}
