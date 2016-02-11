package com.linkedpipes.executor.execution.boundary;

import com.linkedpipes.commons.entities.executor.ExecutionStatus;

import java.io.File;

import com.linkedpipes.commons.entities.executor.MessageSelectList;

/**
 * Represents an execution.
 *
 * @author Škoda Petr
 */
public interface Executor {

    /**
     * Execute task in given directory.
     *
     * @param directory
     * @param id Unique pipeline execution identifier.
     * @return
     */
    public ExecutionStatus execute(File directory, String id);

    /**
     *
     * @return Execution status report.
     */
    public ExecutionStatus getStatus();

    /**
     *
     * @return
     */
    public MessageSelectList getMessages();

}
