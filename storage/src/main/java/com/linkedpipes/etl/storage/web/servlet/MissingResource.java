package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.StorageException;

public class MissingResource extends StorageException {

    public MissingResource(String message, Object... args) {
        super(message, args);
    }
    
}
