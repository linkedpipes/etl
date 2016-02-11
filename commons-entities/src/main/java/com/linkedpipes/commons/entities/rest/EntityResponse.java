package com.linkedpipes.commons.entities.rest;

/**
 *
 * @author Škoda Petr
 * @param <T>
 */
public class EntityResponse<T> extends BaseResponse {

    private T payload;

    public EntityResponse() {
    }

    public EntityResponse(T payload) {
        this.payload = payload;
    }

    public EntityResponse(RestException exception) {
        super(exception);
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

}
