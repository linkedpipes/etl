package com.linkedpipes.plugin.loader.wikibase;

final class WikibaseLoaderVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/l-wikibase#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_ONTOLOGY_IRI = PREFIX + "ontologyIriBase";

    public static final String HAS_INSTANCE_IRI = PREFIX + "instanceIriBase";

    public static final String HAS_EDIT_TIME = PREFIX + "averageTimePerEdit";

    private WikibaseLoaderVocabulary() {
    }

}
