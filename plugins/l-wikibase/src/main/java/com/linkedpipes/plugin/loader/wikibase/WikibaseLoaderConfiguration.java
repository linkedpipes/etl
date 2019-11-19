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

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_SITE_IRI)
    private String siteIri;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_SPARQL_URL)
    private String sparqlUrl;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_REF_PROPERTY)
    private String refProperty;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_EDIT_TIME)
    private int averageTimePerEdit = 2000;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_STRICT_MATCHING)
    private boolean strictMatching = false;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_SKIP_ON_ERROR)
    private boolean skipOnError = false;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_NEW_ITEM_MESSAGE)
    private String newItemMessage = "Create new entity.";

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_REPLACE_ITEM_MESSAGE)
    private String replaceItemMessage = "Replace entity.";

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_UPDATE_ITEM_MESSAGE)
    private String mergeItemMessage = "Update entity.";

    @RdfToPojo.Property(
            iri = WikibaseLoaderVocabulary.HAS_UPDATE_BY_REPLACE_ITEM_MESSAGE)
    private String mergeByReplaceItemMessage = "Update by replace.";

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

    public String getSiteIri() {
        return siteIri;
    }

    public void setSiteIri(String siteIri) {
        this.siteIri = siteIri;
    }

    public String getSparqlUrl() {
        return sparqlUrl;
    }

    public void setSparqlUrl(String sparqlUrl) {
        this.sparqlUrl = sparqlUrl;
    }

    public String getRefProperty() {
        return refProperty;
    }

    public void setRefProperty(String refProperty) {
        this.refProperty = refProperty;
    }

    public int getAverageTimePerEdit() {
        return averageTimePerEdit;
    }

    public void setAverageTimePerEdit(int averageTimePerEdit) {
        this.averageTimePerEdit = averageTimePerEdit;
    }

    public boolean isStrictMatching() {
        return strictMatching;
    }

    public void setStrictMatching(boolean strictMatching) {
        this.strictMatching = strictMatching;
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    public String getNewItemMessage() {
        return newItemMessage;
    }

    public void setNewItemMessage(String newItemMessage) {
        this.newItemMessage = newItemMessage;
    }

    public String getReplaceItemMessage() {
        return replaceItemMessage;
    }

    public void setReplaceItemMessage(String replaceItemMessage) {
        this.replaceItemMessage = replaceItemMessage;
    }

    public String getMergeItemMessage() {
        return mergeItemMessage;
    }

    public void setMergeItemMessage(String mergeItemMessage) {
        this.mergeItemMessage = mergeItemMessage;
    }

    public String getMergeByReplaceItemMessage() {
        return mergeByReplaceItemMessage;
    }

    public void setMergeByReplaceItemMessage(String mergeByReplaceItemMessage) {
        this.mergeByReplaceItemMessage = mergeByReplaceItemMessage;
    }

}
