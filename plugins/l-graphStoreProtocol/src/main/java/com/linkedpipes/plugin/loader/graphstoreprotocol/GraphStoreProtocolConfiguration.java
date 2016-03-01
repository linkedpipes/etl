package com.linkedpipes.plugin.loader.graphstoreprotocol;

import com.linkedpipes.etl.dpu.api.rdf.RdfToPojo;

/**
 *
 * @author Petr Škoda
 */
@RdfToPojo.Type(uri = GraphStoreProtocolVocabulary.CONFIG_CLASS)
public class GraphStoreProtocolConfiguration {

    public static enum RepositoryType {
        Virtuoso,
        Fuseki,
        /**
         * Experimental for Fuseki 2.+ TRIG.
         */
        FusekiTrig
    }

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_SELECT)
    private String endpointSelect;

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_UPDATE)
    private String endpointUpdate;

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_CRUD)
    private String endpointCRUD;

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_GRAPH)
    private String targetGraphURI;

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_TYPE)
    private RepositoryType repositoryType = RepositoryType.Fuseki;

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_AUTH)
    private boolean useAuthentification = true;

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_USER)
    private String userName = "";

    @RdfToPojo.Property(uri = GraphStoreProtocolVocabulary.HAS_PASSWORD)
    private String password = "";

    public GraphStoreProtocolConfiguration() {

    }

    public String getEndpointSelect() {
        return endpointSelect;
    }

    public void setEndpointSelect(String endpointSelect) {
        this.endpointSelect = endpointSelect;
    }

    public String getEndpointUpdate() {
        return endpointUpdate;
    }

    public void setEndpointUpdate(String endpointUpdate) {
        this.endpointUpdate = endpointUpdate;
    }

    public String getEndpointCRUD() {
        return endpointCRUD;
    }

    public void setEndpointCRUD(String endpointCRUD) {
        this.endpointCRUD = endpointCRUD;
    }

    public String getTargetGraphURI() {
        return targetGraphURI;
    }

    public void setTargetGraphURI(String targetGraphURI) {
        this.targetGraphURI = targetGraphURI;
    }

    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
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
}
