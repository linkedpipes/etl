package com.linkedpipes.plugin.loader.sparql.endpoint;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = SparqlEndpointLoaderVocabulary.CONFIG_CLASS)
public class SparqlEndpointLoaderConfiguration {

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_AUTH)
    private boolean useAuthentification = true;

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_USERNAME)
    private String userName;

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_CLEAR_GRAPH)
    private boolean clearDestinationGraph = false;

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_TAGET_GRAPH)
    private String targetGraphName;

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_COMMIT_SIZE)
    private int commitSize = 100000;

    public SparqlEndpointLoaderConfiguration() {
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
