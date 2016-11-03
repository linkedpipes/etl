package com.linkedpipes.plugin.transformer.mustache;

/**
 *
 */
public final class MustacheVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-mustache#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_CLASS = PREFIX + "class";

    public static final String HAS_TEMPLATE = PREFIX + "template";

    public static final String HAS_ORDER = PREFIX + "order";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    private MustacheVocabulary() {
    }

}
