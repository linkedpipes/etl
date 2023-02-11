package com.linkedpipes.etl.executor.api.v1.component.task;

/**
 * A single task that can be executed.
 */
public interface Task {

    /**
     * Task identification, should be IRI.
     */
    String getIri();

    /**
     * Group identification. Should there be no group, this function should
     * return constant value. This function must not return null.
     */
    String getGroup();

}
