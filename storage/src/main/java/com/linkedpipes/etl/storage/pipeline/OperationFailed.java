package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.StorageException;

class OperationFailed extends StorageException {

    OperationFailed(String message, Object... args) {
        super(message, args);
    }

}
