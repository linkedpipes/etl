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
        LOG.info("Skipping starts for: {}", this.componentExecution.getIri());
        // No operation here, two logs are here just to be
        // consistent with other executors.
        LOG.info("Skipping end for: {}", this.componentExecution.getIri());
    }

    @Override
    public boolean unexpectedTermination() {
        return false;
    }

}
