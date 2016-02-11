package com.linkedpipes.commons.entities.rest;

/**
 *
 * @author Škoda Petr
 */
public class BaseResponse {

    private RestException exception;

    public BaseResponse() {
    }

    public BaseResponse(RestException exception) {
        this.exception = exception;
    }

    public RestException getException() {
        return exception;
    }

    public void setException(RestException exception) {
        this.exception = exception;
    }

}
