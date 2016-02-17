package com.linkedpipes.etl.executor.api.v1.exception;

/**
 * Store localized string, ie. string with language tag.
 * 
 * @author Petr Škoda
 */
public final class LocalizedString {

    private final String language;

    private final String value;

    public LocalizedString(String language, String value) {
        this.language = language;
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }

}
