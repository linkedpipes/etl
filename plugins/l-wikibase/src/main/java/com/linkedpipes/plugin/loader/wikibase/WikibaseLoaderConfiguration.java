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

}
