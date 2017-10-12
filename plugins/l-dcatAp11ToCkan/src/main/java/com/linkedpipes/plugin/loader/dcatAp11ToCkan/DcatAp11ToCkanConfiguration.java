package com.linkedpipes.plugin.loader.dcatAp11ToCkan;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = DcatAp11ToCkanConfigVocabulary.CONFIG_CLASS)
public class DcatAp11ToCkanConfiguration {

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.API_URL)
    private String apiUri;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.API_KEY)
    private String apiKey;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.DATASET_ID)
    private String datasetID;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.LOAD_LANGUAGE)
    private String loadLanguage;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.PROFILE)
    private String profile;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.VOID_EXAMPLE_RESOURCES)
    private Boolean voidExampleResources;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.VOID_SPARQL_ENDPOINT)
    private Boolean voidSparqlEndpoint;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.OVERRIDE_CKAN_ORGANIZATION)
    private Boolean overrideCkanOrganization;

    @RdfToPojo.Property(iri = DcatAp11ToCkanConfigVocabulary.CKAN_ORGANIZATION)
    private String ckanOrganization;

    public DcatAp11ToCkanConfiguration() {
    }

    public String getApiUri() {
        return apiUri;
    }

    public void setApiUri(String apiUri) {
        this.apiUri = apiUri;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
    }

    public String getLoadLanguage() {
        return loadLanguage;
    }

    public void setLoadLanguage(String loadLanguage) {
        this.loadLanguage = loadLanguage;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Boolean getVoidExampleResources() {
        return voidExampleResources;
    }

    public void setVoidExampleResources(Boolean voidExampleResources) {
        this.voidExampleResources = voidExampleResources;
    }

    public Boolean getVoidSparqlEndpoint() {
        return voidSparqlEndpoint;
    }

    public void setVoidSparqlEndpoint(Boolean voidSparqlEndpoint) {
        this.voidSparqlEndpoint = voidSparqlEndpoint;
    }

    public Boolean getOverrideCkanOrganization() {
        return overrideCkanOrganization;
    }

    public void setOverrideCkanOrganization(Boolean overrideCkanOrganization) {
        this.overrideCkanOrganization = overrideCkanOrganization;
    }

    public String getCkanOrganization() {
        return ckanOrganization;
    }

    public void setCkanOrganization(String ckanOrganization) {
        this.ckanOrganization = ckanOrganization;
    }
}
