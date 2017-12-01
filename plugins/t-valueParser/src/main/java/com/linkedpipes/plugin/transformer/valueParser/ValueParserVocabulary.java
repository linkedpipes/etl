package com.linkedpipes.plugin.transformer.valueParser;

final class ValueParserVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-valueParser#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_REGEXP = PREFIX + "regexp";

    public static final String HAS_METADATA = PREFIX + "preserveMetadata";

    public static final String HAS_SOURCE = PREFIX + "source";

    public static final String BINDING = PREFIX + "Binding";

    public static final String HAS_BINDING = PREFIX + "binding";

    public static final String HAS_GROUP = PREFIX + "group";

    public static final String HAS_TARGET = PREFIX + "target";

    public static final String HAS_TYPE = PREFIX + "type";

    public static final String HAS_ORDER = PREFIX + "order";

    private ValueParserVocabulary() {
    }

}
