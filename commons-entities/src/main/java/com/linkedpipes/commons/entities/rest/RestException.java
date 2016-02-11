package com.linkedpipes.commons.entities.rest;

/**
 *
 * @author Škoda Petr
 */
public class RestException {

    public static enum Codes {
        /**
         * Server is not available.
         */
        CONNECTION_REFUSED,
        /**
         * Try again later.
         */
        RETRY,
        /**
         * Error caused by invalid input.
         */
        INVALID_INPUT,
        /**
         *
         */
        ERROR
    }

    /**
     * Text of exception of detailed description of message.
     */
    private String errorMessage;

    /**
     * Error message for developer.
     */
    private String systemMessage;

    /**
     * Error message for user.
     */
    private String userMessage;

    /**
     * Error codes:
     */
    private Codes errorCode;

    /**
     * Cause of exception.
     */
    private RestException source;

    public RestException() {
    }

    public RestException(String errorMessage, String systemMessage, String userMessage, Codes errorCode) {
        this.errorMessage = errorMessage;
        this.systemMessage = systemMessage;
        this.userMessage = userMessage;
        this.errorCode = errorCode;
    }

    public RestException(String errorMessage, String systemMessage, String userMessage, Codes errorCode, RestException source) {
        this.errorMessage = errorMessage;
        this.systemMessage = systemMessage;
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.source = source;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public Codes getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Codes errorCode) {
        this.errorCode = errorCode;
    }

    public RestException getSource() {
        return source;
    }

    public void setSource(RestException source) {
        this.source = source;
    }

}
