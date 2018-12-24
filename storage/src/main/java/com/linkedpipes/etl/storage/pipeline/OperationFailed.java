package com.linkedpipes.etl.storage.pipeline;

import com.linkedpipes.etl.storage.BaseException;

class OperationFailed extends BaseException {

    OperationFailed(String message, Object... args) {
        super(message, args);
    }

}
