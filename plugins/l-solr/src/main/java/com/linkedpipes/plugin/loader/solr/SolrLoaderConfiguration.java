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

}
