package com.linkedpipes.etl.dpu.api.extensions;

import java.util.Collection;

/**
 *
 * @author Škoda Petr
 */
public interface ProgressReport {

    /**
     *
     * @param reportStepSuggestion After how many steps should be the progress reported.
     */
    public void startTotalUnknown(int reportStepSuggestion);

    public void start(int entriesToProcess);

    public void start(Collection<?> collection);

    public void entryProcessed();

    public void done();

}
