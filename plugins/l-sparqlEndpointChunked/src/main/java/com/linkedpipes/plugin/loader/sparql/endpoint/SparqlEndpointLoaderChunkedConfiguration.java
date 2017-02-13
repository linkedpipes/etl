package com.linkedpipes.plugin.loader.sparql.endpoint;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SparqlEndpointLoaderChunkedVocabulary.CONFIG)
public class SparqlEndpointLoaderChunkedConfiguration {

    @RdfToPojo.Property(
            iri = SparqlEndpointLoaderChunkedVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(iri = SparqlEndpointLoaderChunkedVocabulary.HAS_AUTH)
    private boolean useAuthentification = true;

    @RdfToPojo.Property(
            iri = SparqlEndpointLoaderChunkedVocabulary.HAS_USERNAME)
    private String userName;

    @RdfToPojo.Property(
            iri = SparqlEndpointLoaderChunkedVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(
            iri = SparqlEndpointLoaderChunkedVocabulary.HAS_CLEAR_GRAPH)
    private boolean clearDestinationGraph = false;

    @RdfToPojo.Property(
            iri = SparqlEndpointLoaderChunkedVocabulary.HAS_TAGET_GRAPH)
    private String targetGraphName;

    @RdfToPojo.Property(
            iri = SparqlEndpointLoaderChunkedVocabulary.HAS_COMMIT_SIZE)
    private int commitSize = 100000;

    public SparqlEndpointLoaderChunkedConfiguration() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isUseAuthentification() {
        return useAuthentification;
    }

    public void setUseAuthentification(boolean useAuthentification) {
        this.useAuthentification = useAuthentification;
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
