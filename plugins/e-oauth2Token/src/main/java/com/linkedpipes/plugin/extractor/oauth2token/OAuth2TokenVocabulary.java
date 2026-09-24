package com.linkedpipes.plugin.extractor.oauth2token;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

final class OAuth2TokenVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/e-oauth2Token#";

    private static final String RESOURCE
            = "http://plugins.linkedpipes.com/resource/e-oauth2Token/";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_PROVIDER = PREFIX + "provider";

    public static final String HAS_TENANT = PREFIX + "tenant";

    public static final String HAS_CLIENT_ID = PREFIX + "clientId";

    public static final String HAS_CLIENT_SECRET = PREFIX + "clientSecret";

    public static final String HAS_SCOPE = PREFIX + "scope";

    public static final String HAS_TOKEN_ENDPOINT = PREFIX + "tokenEndpoint";

    public static final IRI ACCESS_TOKEN_CLASS;

    public static final IRI HAS_ACCESS_TOKEN;

    public static final IRI HAS_TOKEN_TYPE;

    public static final IRI HAS_EXPIRES_IN;

    public static final IRI ACCESS_TOKEN_RESOURCE;

    static {
        final ValueFactory valueFactory = SimpleValueFactory.getInstance();
        ACCESS_TOKEN_CLASS = valueFactory.createIRI(PREFIX + "AccessToken");
        HAS_ACCESS_TOKEN = valueFactory.createIRI(PREFIX + "accessToken");
        HAS_TOKEN_TYPE = valueFactory.createIRI(PREFIX + "tokenType");
        HAS_EXPIRES_IN = valueFactory.createIRI(PREFIX + "expiresIn");
        ACCESS_TOKEN_RESOURCE =
                valueFactory.createIRI(RESOURCE + "accessToken");
    }

    private OAuth2TokenVocabulary() {
    }

}
