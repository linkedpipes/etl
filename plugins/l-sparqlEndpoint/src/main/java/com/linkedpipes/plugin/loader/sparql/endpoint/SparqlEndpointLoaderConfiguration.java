package com.linkedpipes.plugin.loader.sparql.endpoint;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;

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
    private String username = "dba";

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_PASSWORD)
    private String password = "dba";

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_CLEAR_GRAPH)
    private boolean clearDestinationGraph = false;

    @RdfToPojo.Property(uri = SparqlEndpointLoaderVocabulary.HAS_TAGET_GRAPH)
    private String targetGraphName = "";

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
