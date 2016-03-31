package com.linkedpipes.etl.executor.component;

/**
 * Execute component with "SKIP" execution type.
 *
 * @author Petr Škoda
 */
class SkipComponent implements ComponentExecutor {

    SkipComponent() {
    }

    @Override
    public void execute() {
        // No action here.
    }

    @Override
    public void cancel() {
        // No action here.
    }

    @Override
    public boolean unexpectedTermination() {
        return false;
    }

}
