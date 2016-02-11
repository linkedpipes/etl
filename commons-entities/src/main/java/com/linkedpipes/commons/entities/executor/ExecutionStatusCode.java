package com.linkedpipes.commons.entities.executor;

/**
 *
 * @author Škoda Petr
 */
public enum ExecutionStatusCode {

    QUEUED(100),
    INITIALIZING(200),
    INITIALIZATION_FAILED(300),
    RUNNING(400),
    FINISHED(500),
    FAILED(600),
    FAILED_ON_THROWABLE(700);

    private final int code;

    private ExecutionStatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
