package com.linkedpipes.etl.rdf.utils.pojo;

import com.linkedpipes.etl.rdf.utils.RdfUtilsException;

public class LoaderException extends RdfUtilsException {

    public LoaderException(String messages, Object... args) {
        super(messages, args);
    }

}