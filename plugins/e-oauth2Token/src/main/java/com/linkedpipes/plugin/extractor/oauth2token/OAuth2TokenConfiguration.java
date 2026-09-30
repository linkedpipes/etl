package com.linkedpipes.plugin.extractor.oauth2token;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = OAuth2TokenVocabulary.CONFIG)
public class OAuth2TokenConfiguration {

    public enum Provider {
        ENTRA
    }

    @RdfToPojo.Property(iri = OAuth2TokenVocabulary.HAS_PROVIDER)
    private Provider provider = Provider.ENTRA;

    @RdfToPojo.Property(iri = OAuth2TokenVocabulary.HAS_TENANT)
    private String tenant = "";

    @RdfToPojo.Property(iri = OAuth2TokenVocabulary.HAS_CLIENT_ID)
    private String clientId = "";

    @RdfToPojo.Property(iri = OAuth2TokenVocabulary.HAS_CLIENT_SECRET)
    private String clientSecret = "";

    @RdfToPojo.Property(iri = OAuth2TokenVocabulary.HAS_SCOPE)
    private String scope = "";

    @RdfToPojo.Property(iri = OAuth2TokenVocabulary.HAS_TOKEN_ENDPOINT)
    private String tokenEndpoint = "";

    public OAuth2TokenConfiguration() {
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

}
