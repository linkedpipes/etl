package com.linkedpipes.plugin.loader.wikibase;

public final class WikibaseLoaderVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-wikibase#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_SITE_IRI = PREFIX + "siteIri";

    public static final String HAS_SPARQL_URL = PREFIX + "sparqlUrl";

    public static final String HAS_REF_PROPERTY = PREFIX + "referenceProperty";

    public static final String HAS_EDIT_TIME = PREFIX + "averageTimePerEdit";

    public static final String WIKIDATA_MAPPING = PREFIX + "mappedTo";

    public static final String HAS_STRICT_MATCHING = PREFIX + "strictMatching";

    public static final String HAS_SKIP_ON_ERROR = PREFIX + "skipOnError";

    public static final String NEW_STRATEGY = PREFIX + "New";

    public static final String REPLACE_STRATEGY = PREFIX + "Replace";

    public static final String DELETE_STRATEGY = PREFIX + "Delete";

    public static final String MERGE_STRATEGY = PREFIX + "Merge";

    public static final String EXACT_STRATEGY = PREFIX + "Exact";

    public static final String HAS_NEW_ITEM_MESSAGE  =
            PREFIX + "newItemMessage";

    public static final String HAS_UPDATE_ITEM_MESSAGE  =
            PREFIX + "updateItemMessage";

    public static final String HAS_RETRY_COUNT = PREFIX + "retryCount";

    public static final String HAS_RETRY_PAUSE = PREFIX + "retryPause";

    private WikibaseLoaderVocabulary() {
    }

}
