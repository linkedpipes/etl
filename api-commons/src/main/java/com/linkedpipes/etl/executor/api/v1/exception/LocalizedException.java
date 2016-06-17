package com.linkedpipes.etl.executor.api.v1.exception;

import java.util.List;
import org.slf4j.helpers.MessageFormatter;

/**
 * Exception with support for localisation.
 *
 * The reference of arguments in message should by done by '{}' string.
 * The cause exception should be given as the last argument, if the cause
 * exception is available.
 *
 * @author Petr Škoda
 */
public class LocalizedException extends Exception {

    /**
     * Used to store a string in a given language.
     */
    public static final class Message {

        /**
         * Language tag.
         */
        private final String language;

        /**
         * Message, support '{}' as value place holders.
         */
        private final String value;

        public Message(String value, String language) {
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
     * For each language a message with place holders for arguments.
     */
    protected final List<Message> messages;

    /**
     * Arguments referenced from the message.
     */
    protected final Object[] args;

    public LocalizedException(List<Message> messages, Object... args) {
        // For now use first message as representation
        // of this exception.
        super(messages.get(0).value);
        // Initialize exception.
        if (args.length > 0) {
            if (args[args.length - 1] instanceof Exception) {
                this.initCause((Exception) args[args.length - 1]);
            }
        }
        this.messages = messages;
        this.args = args;
    }

    @Override
    public String getMessage() {
        // Use first given message if it exists.
        if (messages.isEmpty()) {
            return "No message provided!";
        }
        final Message message = messages.iterator().next();
        return MessageFormatter.arrayFormat(message.value, args).getMessage();
    }

}
