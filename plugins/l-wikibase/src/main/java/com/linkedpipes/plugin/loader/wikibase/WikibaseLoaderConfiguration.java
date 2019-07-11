package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = WikibaseLoaderVocabulary.CONFIG)
public class WikibaseLoaderConfiguration {

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_USERNAME)
    private String userName;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_ONTOLOGY_IRI)
    private String ontologyIriBase;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_INSTANCE_IRI)
    private String instanceIriBase;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_EDIT_TIME)
    private int averageTimePerEdit = 2000;

    public WikibaseLoaderConfiguration() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public String getOntologyIriBase() {
        return ontologyIriBase;
    }

    public void setOntologyIriBase(String ontologyIriBase) {
        this.ontologyIriBase = ontologyIriBase;
    }

    public String getInstanceIriBase() {
        return instanceIriBase;
    }

    public void setInstanceIriBase(String instanceIriBase) {
        this.instanceIriBase = instanceIriBase;
    }

    public int getAverageTimePerEdit() {
        return averageTimePerEdit;
    }

    public void setAverageTimePerEdit(int averageTimePerEdit) {
        this.averageTimePerEdit = averageTimePerEdit;
    }

}
