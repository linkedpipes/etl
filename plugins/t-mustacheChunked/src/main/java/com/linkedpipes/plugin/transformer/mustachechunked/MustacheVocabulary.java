package com.linkedpipes.plugin.transformer.mustachechunked;

public final class MustacheVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-mustacheChunked#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_CLASS = PREFIX + "class";

    public static final String HAS_TEMPLATE = PREFIX + "template";

    public static final String HAS_ORDER = PREFIX + "order";

    public static final String HAS_FILE_NAME = PREFIX + "fileName";

    public static final String HAS_ADD_FIRST_FLAG = PREFIX + "addFirstFlag";

    public static final String HAS_IS_FIRST = PREFIX + "first";

    public static final String HAS_ESCAPE_FOR_JSON = PREFIX + "escapeForJson";

    private MustacheVocabulary() {
    }

}
