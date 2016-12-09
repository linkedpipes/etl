package com.linkedpipes.plugin.transformer.bingtranslator;

public final class BingTranslatorVocabulary {

    private static final String PREFIX
            = "http://plugins.linkedpipes.com/ontology/t-bingTranslator#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_SUBSCRIPTION_KEY =
            PREFIX + "subscriptionKey";

    public static final String HAS_DEFAULT_LANGUAGE =
            PREFIX + "defaultLanguage";

    public static final String HAS_TARGET_LANGUAGE = PREFIX + "targetLanguage";

    public static final String HAS_SIMPLE_LANG_TAG = PREFIX + "simpleLangTag";

    private BingTranslatorVocabulary() {
    }

}
