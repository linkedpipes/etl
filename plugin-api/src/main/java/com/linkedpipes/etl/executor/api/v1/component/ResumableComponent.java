package com.linkedpipes.etl.executor.api.v1.component;

import com.linkedpipes.etl.executor.api.v1.LpException;

import java.io.File;

/**
 * Resumable components need to perform periodic checkpoints into
 * their working directories.
 *
 * <p>On resume the component is given same input data and old working
 * directory.
 */
public interface ResumableComponent {

    /**
     * Called before component execution, inform component
     * that the execution is being resumed.
     */
    void resumeExecution(File previousWorkingDirectory) throws LpException;

}
