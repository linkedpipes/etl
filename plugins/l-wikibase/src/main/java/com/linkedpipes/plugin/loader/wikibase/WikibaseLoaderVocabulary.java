package com.linkedpipes.plugin.loader.wikibase;

final class WikibaseLoaderVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-wikibase#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_SITE_IRI = PREFIX + "siteIri";

    public static final String HAS_EDIT_TIME = PREFIX + "averageTimePerEdit";

    public static final String WIKIDATA_ENTITY =
            "http://wikiba.se/ontology#Item";

    public static final String WIKIDATA_STATEMENT =
            "http://wikiba.se/ontology#Statement";

    public static final String WIKIDATA_NEW_ENTITY =
            PREFIX + "New";

    public static final String WIKIDATA_DELETE_ENTITY =
            PREFIX  + "Remove";

    public static final String WIKIDATA_MAPPING =
            PREFIX  + "mappedTp";

    private WikibaseLoaderVocabulary() {
    }

}
