package com.linkedpipes.plugin.loader.wikibase;

public final class WikibaseLoaderVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-wikibase#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_SITE_IRI = PREFIX + "siteIri";

    public static final String HAS_EDIT_TIME = PREFIX + "averageTimePerEdit";

    public static final String WIKIDATA_NEW_ENTITY =
            PREFIX + "New";

    public static final String WIKIDATA_DELETE_ENTITY =
            PREFIX  + "Remove";

    public static final String WIKIDATA_MAPPING =
            PREFIX  + "mappedTo";

    private WikibaseLoaderVocabulary() {
    }

}
