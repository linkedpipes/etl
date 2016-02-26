package com.linkedpipes.etl.dpu.api.extensions;

import java.util.Collection;

/**
 * Should be used to report DPU progress. As frequent call of this {@link #entryProcessed()} may cause
 * performance drop, we advise to report after bigger work chunks (files, graphs).
 *
 * @author Škoda Petr
 */
public interface ProgressReport {

    /**
     * Report start of progress report.
     *
     * @param entriesToProcess
     */
    public void start(int entriesToProcess);

    /**
     * Report start of progress report.
     *
     * @param collection
     */
    public void start(Collection<?> collection);

    public void entryProcessed();

    /**
     * Report completion of the task.
     */
    public void done();

}
