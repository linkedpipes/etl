package com.linkedpipes.etl.executor.monitor.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.linkedpipes.etl.executor.monitor.execution.Execution;

public interface ExecutorEventListener {

    /**
     * When execution was found for given executor.
     *
     * @param execution Can be null.
     */
    void onExecutorHasExecution(Execution execution, Executor executor);

    /**
     * When no execution was found for the executor.
     */
    void onExecutorWithoutExecution(Executor executor);

    /**
     * When executor becomes unavailable/unresponsive.
     */
    void onExecutorUnavailable(Executor executor);

    /**
     * When an overview is available.
     */
    void onOverview(Execution execution, JsonNode overview);

}
