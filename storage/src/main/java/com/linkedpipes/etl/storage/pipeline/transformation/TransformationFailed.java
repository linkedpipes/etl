package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.storage.StorageException;

public class TransformationFailed extends StorageException {

    public TransformationFailed(String message, Object... args) {
        super(message, args);
    }

}
