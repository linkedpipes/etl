package com.linkedpipes.etl.rdf.utils;

public class InvalidNumberOfResults extends RdfUtilsException {

    public InvalidNumberOfResults(String messages, Object... args) {
        super(messages, args);
    }

}
