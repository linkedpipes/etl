package com.linkedpipes.plugin.transformer.getotools;

public final class GeoToolsVocabulary {

    private GeoToolsVocabulary() {
    }

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-geoTools#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_TYPE = PREFIX + "type";

    public static final String HAS_COORD = PREFIX + "coord";

    public static final String HAS_COORD_TYPE = PREFIX + "coordType";

    public static final String HAS_DEFAULT_COORD_TYPE
            = PREFIX + "defaultCoordType";

    public static final String HAS_OUTPUT_PREDICATE
            = PREFIX + "outputPredicate";

    public static final String HAS_OUTPUT_COORD_TYPE
            = PREFIX + "outputCoordType";

    public static final String HAS_FAIL_ON_ERROR
            = PREFIX + "failOnError";

}
