package com.linkedpipes.commons.entities.rest;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Škoda Petr
 * @param <T>
 */
public class ListResponse<T> extends BaseResponse {

    private ListMetadata metadata;

    private List<T> payload;

    /**
     * ID's of deleted items, should be included only in case of 'update' response.
     */
    private Set<String> deleted = null;

    public ListResponse() {
    }

    public ListResponse(RestException exception) {
        super(exception);
    }

    public ListResponse(ListMetadata metadata, List<T> data) {
        this.metadata = metadata;
        this.payload = data;
    }

    public ListMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ListMetadata metadata) {
        this.metadata = metadata;
    }

    public List<T> getPayload() {
        return payload;
    }

    public void setPayload(List<T> payload) {
        this.payload = payload;
    }

    public Set<String> getDeleted() {
        return deleted;
    }

    public void setDeleted(Set<String> deleted) {
        this.deleted = deleted;
    }

}
