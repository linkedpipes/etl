package com.linkedpipes.etl.executor.api.v1.rdf;

/**
 * Can be used to load language string with a language tag.
 */
public class LanguageString {

    protected String value;

    protected String language;

    public LanguageString() {
    }

    public LanguageString(String value, String language) {
        this.value = value;
        this.language = language;
    }

    public void setValue(String value, String language) {
        this.value = value;
        this.language = language;
    }

    public String getValue() {
        return value;
    }

    public String getLanguage() {
        return language;
    }

}
