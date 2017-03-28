package com.linkedpipes.plugin.transformer.tabularuv.parser;

public class ParseFailed extends Exception {

    public ParseFailed(String message) {
        super(message);
    }

    public ParseFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
