package com.linkedpipes.etl.storage.pipeline.transformation;

import com.linkedpipes.etl.storage.BaseException;

public class TransformationFailed extends BaseException {

    public TransformationFailed(String message, Object... args) {
        super(message, args);
    }

}
