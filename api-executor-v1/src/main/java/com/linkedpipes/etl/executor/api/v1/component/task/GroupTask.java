package com.linkedpipes.etl.executor.api.v1.component.task;

/**
 * Represent a task that is part of a group.
 */
public interface GroupTask extends Task {

    Object getGroup();

}
