package com.linkedpipes.commons.entities.rest;

import java.util.Date;

/**
 * Common entity used to report progress.
 *
 * @author Škoda Petr
 */
public class Progress {

    private Integer current;

    private Integer total;

    private Date lastChange;

    public Progress() {
    }

    public Progress(Integer current, Integer total, Date lastChange) {
        this.current = current;
        this.total = total;
        this.lastChange = lastChange;
    }


    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

}
