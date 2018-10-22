package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.BaseException;

public class MissingResource extends BaseException {

    public MissingResource(String message, Object... args) {
        super(message, args);
    }
    
}
