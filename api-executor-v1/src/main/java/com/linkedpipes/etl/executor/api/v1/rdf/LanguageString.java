package com.linkedpipes.etl.executor.api.v1.rdf;

import com.linkedpipes.etl.rdf.utils.pojo.LangString;

/**
 * Can be used to load language string with a language tag.
 */
public class LanguageString implements LangString {

    private String value;

    private String language;

    public LanguageString() {
    }

    public LanguageString(String value, String language) {
        this.value = value;
        this.language = language;
    }

    @Override
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
