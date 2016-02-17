package com.linkedpipes.etl.executor.api.v1.exception;

import java.util.List;

/**
 * Exception with support for localization.
 *
 * The reference of arguments in message should by done by '{}' string. The cause exception should be given
 * as the last argument, if the cause exception is available.
 *
 * @author Petr Škoda
 */
public class LocalizedException extends Exception {

    /**
     * Used to store a string in a given language.
     */
    public static final class LocalizedString {

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

    /**
     * For each language a message with placeholders for arguments.
     */
    protected final List<LocalizedString> messages;

    protected final Object[] args;

    public LocalizedException(List<LocalizedString> messages, Object... args) {
        this.messages = messages;
        this.args = args;
    }

}
