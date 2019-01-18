package com.linkedpipes.etl.executor.api.v1.service;

import java.util.Collection;

/**
 * Should be used to report DPU progress. As frequent call of this
 * {@link #entryProcessed()} may cause performance drop, we advise to report
 * after bigger work chunks (files, graphs).
 */
public interface ProgressReport {

    /**
     * Report start of progress report.
     */
    void start(long entriesToProcess);

    /**
     * Report start of progress report.
     */
    void start(Collection<?> collection);

    /**
     * Should be called whenever entry is processed.
     */
    void entryProcessed();

    /**
     * Report completion of the task.
     */
    void done();

}
