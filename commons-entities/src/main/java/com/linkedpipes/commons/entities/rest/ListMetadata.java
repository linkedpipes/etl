package com.linkedpipes.commons.entities.rest;

import java.util.Date;

/**
 *
 * @author Škoda Petr
 */
public class ListMetadata {

    private int count;

    private Date created;

    public ListMetadata() {
    }

    public ListMetadata(int count, Date created) {
        this.count = count;
        this.created = created;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

}
