package com.linkedpipes.commons.entities.executor;

/**
 *
 * @author Škoda Petr
 */
public enum ExecutionStatusCode {

    QUEUED(120),
    INITIALIZING(140),
    INITIALIZATION_FAILED(513),
    RUNNING(160),
    FINISHED(200),
    FAILED(511),
    FAILED_ON_THROWABLE(512);

    private final int code;

    private ExecutionStatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
