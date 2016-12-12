package com.linkedpipes.plugin.transformer.bingtranslator;

import com.linkedpipes.etl.component.api.service.RdfToPojo;

import java.util.LinkedList;
import java.util.List;

@RdfToPojo.Type(uri = BingTranslatorVocabulary.CONFIG)
public class BingTranslatorConfiguration {

    @RdfToPojo.Property(uri = BingTranslatorVocabulary.HAS_SUBSCRIPTION_KEY)
    private String subscriptionKey;

    /**
     * Default language, used for literal without language.
     */
    @RdfToPojo.Property(uri = BingTranslatorVocabulary.HAS_DEFAULT_LANGUAGE)
    private String defaultLanguage;

    /**
     * Target languages to translate into.
     */
    @RdfToPojo.Property(uri = BingTranslatorVocabulary.HAS_TARGET_LANGUAGE)
    private List<String> targetLanguages = new LinkedList<>();

    @RdfToPojo.Property(uri = BingTranslatorVocabulary.HAS_USE_BCP47)
    private boolean useBCP47 = false;

    public BingTranslatorConfiguration() {
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public List<String> getTargetLanguages() {
        return targetLanguages;
    }

    public void setTargetLanguages(List<String> targetLanguages) {
        this.targetLanguages = targetLanguages;
    }

    public boolean isUseBCP47() {
        return useBCP47;
    }

    public void setUseBCP47(boolean useBCP47) {
        this.useBCP47 = useBCP47;
    }
}
