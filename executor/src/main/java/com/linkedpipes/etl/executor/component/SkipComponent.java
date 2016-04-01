package com.linkedpipes.etl.executor.component;

import com.linkedpipes.etl.executor.execution.ExecutionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute component with "SKIP" execution type.
 *
 * @author Petr Škoda
 */
class SkipComponent implements ComponentExecutor {

    private static final Logger LOG
            = LoggerFactory.getLogger(SkipComponent.class);

    private final ExecutionModel.Component componentExecution;

    SkipComponent(ExecutionModel.Component componentExecution) {
        this.componentExecution = componentExecution;
    }

    @Override
    public void execute() {
        LOG.info("Skiping starts for: {}", this.componentExecution.getIri());
        // No operation here.
        LOG.info("Skiping end for: {}", this.componentExecution.getIri());
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
