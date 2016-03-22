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
        // No aciton here.
    }

    @Override
    public void cancel() {
        // No aciton here.
    }

}
