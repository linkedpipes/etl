package com.linkedpipes.plugin.loader.wikibase;

final class WikibaseLoaderVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-wikibase#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_SITE_IRI = PREFIX + "siteIri";

    private WikibaseLoaderVocabulary() {
    }

}
